(ns com.phronemophobic.add-deps2
  (:require [clojure.string :as str]
            [com.rpl.specter :as specter]
            [clojure.pprint
             :refer [pprint]]
            [borkdude.rewrite-edn :as r]
            [membrane.ui :as ui]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            clojure.set
            [membrane.basic-components :as basic]
            [membrane.component :refer [defeffect
                                        defui]
             :as component]
            [membrane.skia :as backend]
            membrane.skia.paragraph
            [clojure.java.shell :as sh]
            [clojure.tools.deps :as deps]
            [clojure.repl.deps :as repl.deps]
            [com.phronemophobic.membrane.tableview
             :as tv]

            [clojure.edn :as edn])
  (:import java.io.PushbackReader
           java.util.zip.GZIPInputStream)
  (:gen-class))

(defn gzip-stream [is]
  (GZIPInputStream. is))

(let [name->lib (memoize
                 (fn [libs]
                   (into {}
                         (map (juxt :name identity))
                         libs)))]
  (defn get-by-name [libs name]
    ((name->lib libs) name)))

(defn filter-table [filt selection search-text libs ]
  (let [libs (case filt
               :all libs
               :installed (vals (:installed selection))
               :change
               (let [{:keys [remove add installed]} selection]
                 (into []
                       (concat
                        (keep (fn [lib]
                                (get installed lib))
                              remove)
                        (map (fn [lib-name]
                               (get-by-name libs lib-name))
                             add)))))]
    (->> libs
         (filter #(or
                   (when-let [desc (:description % "")]
                     (str/includes? desc search-text))
                   (str/includes? (-> % :artifact-id name) search-text)))
         (sort-by :score)
         reverse
         (map
          (fn [m]
            (into []
                  (map (fn [k]
                         [m k]))
                  [:group-id
                   :artifact-id
                   :description
                   :score
                   :url])))))
  )

(defn truncate [s n]
  (if (> (count s) n)
    (subs s 0 n)
    s))

(defn initial-selection []
  (let [basis (deps/create-basis {})
        top-deps (into {}
                       (comp (filter (fn [[lib m]]
                                       (empty? (sequence cat (:parents m)))))
                             (map (fn [[lib m]]
                                    [lib
                                     (assoc m
                                            :name lib
                                            :artifact-id (name lib)
                                            :group-id (namespace lib))])))
                       (:libs basis))]
    {:installed top-deps
     :add #{}
     :remove #{}}))

(defn read-edn-gz [url]
  (with-open [is (io/input-stream (io/as-url url))
              gz (GZIPInputStream. is)
              rdr (io/reader gz)
              rdr (PushbackReader. rdr)]
    (edn/read rdr)))


(def star-factor 70)
(def releases-url "https://api.github.com/repos/phronmophobic/dewey/releases/latest")
(defn latest-git-libs []
  (let [release-info (json/read-str (slurp (io/as-url releases-url)))
        release-url (->
                     release-info
                     (get "assets")
                     (->>
                      (filter (fn [p]
                                (= "deps-libs.edn.gz" (get p "name")))))
                     first
                     (get "browser_download_url"))
        git-libs (read-edn-gz release-url)
        git-libs (->> git-libs
                      (map (fn [[nm m]]
                             (assoc m
                                    :name nm
                                    :score  (* star-factor
                                               (:stars m 0))
                                    :group-id (namespace nm)
                                    :artifact-id (name nm)))))]
    git-libs))

(defn latest-clojars-libs []
  (let [
        stats (edn/read-string
               (slurp (io/as-url "https://repo.clojars.org/stats/all.edn")))
        downloads (->>
                   stats
                   (into {} (map (fn [[lib stats]] [lib (reduce + (vals stats))]))))
        feed-str (with-open
                   [is
                    (io/input-stream
                     (io/as-url "https://clojars.org/repo/feed.clj.gz"))
                    gz
                    (gzip-stream is)]
                   (slurp gz))
        libs (->>
              feed-str
              clojure.string/split-lines
              (map edn/read-string)
              (map (fn [p]
                     (assoc p
                            :name (symbol (:group-id p)
                                          (:artifact-id p)))))
              (map
               (fn [p]
                 (assoc p
                        :downloads
                        (or
                         (downloads [(:group-id p) (:artifact-id p)])
                         0))))
              (map (fn [p]
                     (assoc p
                            :score (:downloads p))))
              (map (fn [m]
                     (assoc m
                            :versions
                            (map
                             (fn [p] (do #:mvn{:version p}))
                             (:versions m))))))]
    libs)
  )

(comment

  (def clojibs (latest-clojars-libs))
  (def gitlibs (latest-git-libs))
  ,)

(defn initial-state []
  (let [deps-libs (into []
                        cat
                        [(latest-clojars-libs)
                         (latest-git-libs)])
        state
        {:search-text ""
         :filt :all
         :status ""
         :page-size 15
         :page 0
         :selection (initial-selection)
         :data (->> deps-libs
                    (filter (fn [m]
                              (seq (:versions m)))))}]
    (assoc state
           :total-pages (max 1
                             (max 1
                                  (inc
                                   (int
                                    (/
                                     (count (:data state))
                                     (:page-size state)))))))))


(comment

  (require '[com.phronemophobic.membrane.schematic3
             :as schematic])

  (require '[clojure.repl.deps :refer [add-lib]])
  (require '[clojure.tools.deps :as deps])
  (def my-basis (deps/create-basis {}))

  (require '[clojure.repl.deps :refer [add-lib]
             :as repl])
  (deps/add-lib 'hiccup/hiccup)
  (deps/add-libs 'hiccup/hiccup)

  (deps/sync-deps)

  ,)



(defeffect ::next-page [$page total-pages]
  (dispatch! :update $page
             (fn [p]
               (min (dec total-pages)
                    (inc p)))))

(defeffect ::prev-page [$page]
  (dispatch! :update $page (fn [p]
                             (max 0
                                  (dec p)))))

;; needed for updating deps in thread.
(def ^:private main-class-loader @clojure.lang.Compiler/LOADER)

(defeffect ::set-status [s]
  (dispatch! :set (specter/path :status)
             (truncate s 40)))

(defn add-dep [dep coord]
  (let [edn-string (slurp "deps.edn")
        nodes (r/parse-string edn-string)]
    (spit "deps.edn" (str (r/assoc-in nodes [:deps dep] coord)))))

(defn add-deps [dep-coords]
  (let [edn-string (slurp "deps.edn")
        nodes (r/parse-string edn-string)
        nodes (reduce (fn [nodes [dep coord]]
                        (r/assoc-in nodes [:deps dep] coord))
                      nodes
                      dep-coords)]
    (spit "deps.edn" (str nodes))))


(defeffect ::apply-updates [$selection]
  (future
    (.setContextClassLoader (Thread/currentThread) main-class-loader)
    (dispatch! ::set-status "applying updates")
    (try
      (let [selection (dispatch! :get $selection)

            edn-string (slurp "deps.edn")
            nodes (r/parse-string edn-string)
            libs (dispatch! :get (specter/path :data))

            ;; add
            lib-coords
            (into {}
                  (map (fn [name]
                         (let [lib (get-by-name libs name)
                               coord (-> lib
                                         :versions
                                         first)
                               coord (if (:mvn/version coord)
                                       coord
                                       (assoc coord
                                              :git/url (:url lib)))]
                           [(:name lib)
                            coord])))
                  (:add selection))
            nodes (reduce (fn [nodes [dep coord]]
                            (r/assoc-in nodes [:deps dep] coord))
                          nodes
                          lib-coords)
            ;; remove
            nodes (reduce (fn [nodes dep]
                            (r/update nodes :deps
                                      #(r/dissoc % dep)))
                          nodes
                          (:remove selection))]
        ;; actually applies deps in repl
        (deps/create-basis {:project (edn/read-string (str nodes))})
        ;; update deps.edn file if successful
        (spit "deps.edn" (str nodes))
        (let [selection (initial-selection)]
          (dispatch! :set
                     $selection
                     selection))
        (dispatch! ::set-status ""))
      (catch Throwable e
        (pprint e)
        (dispatch! ::set-status (str e))))))

(defn toggle [s x]
  (if (contains? s x)
    (disj s x)
    (conj s x)))

(defeffect ::select-lib [$selection lib]
  (dispatch! :update
             $selection
             (fn [selection]
               (let [k (if (contains? (:installed selection) lib)
                         :remove
                         :add)]
                 (update-in selection [k] toggle lib)))))

(defn my-ccground [selection $selection cell [w h]]
  (let [
        lib (-> cell
                    meta
                    :lib)
        lib-name (:name lib)
        body (ui/fixed-bounds [w h]
                              cell)
        bg-color (cond
                   (contains? (:add selection) lib-name)
                   [0 0.9 0]

                   (contains? (:remove selection) lib-name)
                   [1 0.35 0.35]

                   (contains? (:installed selection) lib-name)
                   [0.9 0.9 0.9])
        body (if bg-color
               (ui/fill-bordered bg-color
                                 0
                                 body)
               body)]
   (ui/on
    :mouse-down
    (fn [_]
      [[::select-lib $selection lib-name]])
    body)))




(defn label-content [datum]
  (ui/label (truncate (str datum)
                      60)))

(defn my-ccontent [[m f]]
  (with-meta
    (label-content (f m))
    {:lib m}))

(defui my-tableview [{:keys [data selection page page-size]}]
  (let [chunk (->> data
                   (drop (* page page-size))
                   (take page-size))]
   (tv/tableview chunk
                 (partial my-ccground selection $selection)
                 my-ccontent
                 (tv/pad tv/max-col-width 8)
                 (tv/pad tv/max-row-height 8))))

(defui maybe-button [{:keys [show? text]}]
  (when show?
    (basic/button {:text text})))


(defn update-page-info [state]
  (assoc state
         :page 0
         :total-pages (max 1
                           (inc
                            (int
                             (/
                              (count
                               (filter-table
                                (:filt state)
                                (:selection state)
                                (:search-text state)
                                (:data state)))
                              (:page-size state)))))
         ))

(comment
  (def app-state (atom (initial-state)))
  (add-watch app-state
             ::adjust-search
             (fn [k ref old new]
               (when (not= (:filt old)
                           (:filt new))
                 (swap! ref
                        (fn [state]
                          (-> state
                              (assoc :search-text "")
                              (update-page-info)))))))

  (add-watch app-state
             ::set-page
             (fn [k ref old new]
               (when (not= (:search-text old)
                           (:search-text new))
                 (swap! ref update-page-info))))

  (def app (component/make-app #'deps-app app-state))
  
  (let [[w h] (ui/bounds (app))]
    (backend/run app
      {:window-start-width (+ w 4)
       :window-start-height (+ h 4)
       :window-title "Add Deps 2"}))
  ,)





(comment

  (require '[com.phronemophobic.membrane.schematic3
             :as schematic])

;; load data
  (def deps-libs (latest-deps-libs))
  (schematic/load!)

  (schematic/add-component!
   #'my-tableview
   {:data []})

  (schematic/add-component!
   #'maybe-button
   {:show? true
  :text "button"})
  (schematic/show!)

  

  ,)




;; (pprint (schematic/export (:root @schematic/app-state)))
;; Auto generated
(do
 (membrane.component/defui
  pager
  [{:keys [page total-pages]}]
  (clojure.core/when-let
   [elem__31938__auto__
    (clojure.core/apply
     membrane.ui/horizontal-layout
     [(clojure.core/when-let
       [elem__31938__auto__
        (membrane.basic-components/button
         {:text "<",
          :on-click
          (fn [] [[:com.phronemophobic.add-deps2/prev-page $page]])})]
       (clojure.core/with-meta
        elem__31938__auto__
        '#:com.phronemophobic.membrane.schematic3{:ast
                                                  #:element{:id
                                                            #uuid "1e8baa09-986a-40ed-862c-e96f49ef076d"}}))
      (clojure.core/when-let
       [elem__31938__auto__
        (membrane.basic-components/button
         {:text ">",
          :on-click
          (fn
           []
           [[:com.phronemophobic.add-deps2/next-page
             $page
             total-pages]])})]
       (clojure.core/with-meta
        elem__31938__auto__
        '#:com.phronemophobic.membrane.schematic3{:ast
                                                  #:element{:id
                                                            #uuid "f9caf8bf-f2e5-4a3e-864e-d95f9955e328"}}))
      (clojure.core/when-let
       [elem__31938__auto__
        (membrane.ui/translate
         6.9453125
         7.44921875
         (clojure.core/when-let
          [elem__31938__auto__
           (clojure.core/apply
            membrane.ui/horizontal-layout
            [(clojure.core/when-let
              [elem__31938__auto__
               (membrane.skia.paragraph/paragraph
                (str (inc page))
                nil
                nil)]
              (clojure.core/with-meta
               elem__31938__auto__
               '#:com.phronemophobic.membrane.schematic3{:ast
                                                         #:element{:id
                                                                   #uuid "1a9577db-a74f-4680-8d6d-a277eb788295"}}))
             (clojure.core/when-let
              [elem__31938__auto__
               (membrane.skia.paragraph/paragraph "/" nil nil)]
              (clojure.core/with-meta
               elem__31938__auto__
               '#:com.phronemophobic.membrane.schematic3{:ast
                                                         #:element{:id
                                                                   #uuid "c2f25c52-e049-4718-8ce5-71a125088b3b"}}))
             (clojure.core/when-let
              [elem__31938__auto__
               (membrane.skia.paragraph/paragraph
                (str total-pages)
                nil
                nil)]
              (clojure.core/with-meta
               elem__31938__auto__
               '#:com.phronemophobic.membrane.schematic3{:ast
                                                         #:element{:id
                                                                   #uuid "d7f12bce-bd1c-416b-ad30-e2ce3665457a"}}))])]
          (clojure.core/with-meta
           elem__31938__auto__
           '#:com.phronemophobic.membrane.schematic3{:ast
                                                     #:element{:id
                                                               #uuid "8bc05789-3395-4520-9464-fb6a4056ceba"}})))]
       (clojure.core/with-meta
        elem__31938__auto__
        '#:com.phronemophobic.membrane.schematic3{:ast
                                                  #:element{:id
                                                            #uuid "23b769a0-24d2-4e3e-8360-af28df038916"}}))])]
   (clojure.core/with-meta
    elem__31938__auto__
    '#:com.phronemophobic.membrane.schematic3{:ast
                                              #:element{:id
                                                        #uuid "bf23ba79-6d4a-43a2-9c7c-e71292c123b0"}})))
 (membrane.component/defui
  deps-app
  [{:keys
    [page-size
     total-pages
     status
     search-text
     data
     selection
     filt
     page]}]
  (clojure.core/when-let
   [elem__31938__auto__
    [(clojure.core/when-let
      [elem__31938__auto__
       (membrane.ui/translate
        103.67578125
        57.9921875
        (clojure.core/when-let
         [elem__31938__auto__
          (membrane.skia.paragraph/paragraph status nil nil)]
         (clojure.core/with-meta
          elem__31938__auto__
          '#:com.phronemophobic.membrane.schematic3{:ast
                                                    #:element{:id
                                                              #uuid "182a54b8-f4c1-49b9-b8cd-f44de9e462bb"}})))]
      (clojure.core/with-meta
       elem__31938__auto__
       '#:com.phronemophobic.membrane.schematic3{:ast
                                                 #:element{:id
                                                           #uuid "2075d855-b551-4021-a2f2-21a71ef4202e"}}))
     (clojure.core/when-let
      [elem__31938__auto__
       (membrane.ui/translate
        399.21875
        55.73828125
        (clojure.core/when-let
         [elem__31938__auto__
          (clojure.core/apply
           membrane.ui/horizontal-layout
           [(clojure.core/when-let
             [elem__31938__auto__
              (pager {:page page, :total-pages total-pages})]
             (clojure.core/with-meta
              elem__31938__auto__
              '#:com.phronemophobic.membrane.schematic3{:ast
                                                        #:element{:id
                                                                  #uuid "e6e7eb7d-33cc-434a-98c9-f80f71eeaf3d"}}))
            (clojure.core/when-let
             [elem__31938__auto__
              (membrane.ui/on
               :mouse-down
               (fn
                [_]
                [[:com.phronemophobic.add-deps2/apply-updates
                  $selection]])
               [(clojure.core/when-let
                 [elem__31938__auto__
                  (com.phronemophobic.add-deps2/maybe-button
                   {:text "Apply Updates",
                    :show?
                    (or
                     (seq (:add selection))
                     (seq (:remove selection)))})]
                 (clojure.core/with-meta
                  elem__31938__auto__
                  '#:com.phronemophobic.membrane.schematic3{:ast
                                                            #:element{:id
                                                                      #uuid "39dfdd5a-2060-49a0-bd66-b74fe7cf356f"}}))])]
             (clojure.core/with-meta
              elem__31938__auto__
              '#:com.phronemophobic.membrane.schematic3{:ast
                                                        #:element{:id
                                                                  #uuid "25e3b4b0-8fb4-41ca-a4ea-aa9a2530bbc7"}}))])]
         (clojure.core/with-meta
          elem__31938__auto__
          '#:com.phronemophobic.membrane.schematic3{:ast
                                                    #:element{:id
                                                              #uuid "ad5d0e23-51b9-41f9-8741-c2006184afc9"}})))]
      (clojure.core/with-meta
       elem__31938__auto__
       '#:com.phronemophobic.membrane.schematic3{:ast
                                                 #:element{:id
                                                           #uuid "ab4ba23e-d3ec-4ac8-bf8b-390d8ccf4908"}}))
     (clojure.core/when-let
      [elem__31938__auto__
       (membrane.ui/translate 371.20703125 55.890625 nil)]
      (clojure.core/with-meta
       elem__31938__auto__
       '#:com.phronemophobic.membrane.schematic3{:ast
                                                 #:element{:id
                                                           #uuid "c8371a36-b3b3-4005-bab5-e5014ceeacaf"}}))
     (clojure.core/when-let
      [elem__31938__auto__
       (membrane.ui/translate 327.78125 52.734375 nil)]
      (clojure.core/with-meta
       elem__31938__auto__
       '#:com.phronemophobic.membrane.schematic3{:ast
                                                 #:element{:id
                                                           #uuid "dee6176e-4851-4e98-95c1-ce31df03ba24"}}))
     (clojure.core/when-let
      [elem__31938__auto__
       (membrane.ui/translate
        5.8125
        99.09765625
        (clojure.core/when-let
         [elem__31938__auto__
          (membrane.ui/on
           [(clojure.core/when-let
             [elem__31938__auto__
              (com.phronemophobic.add-deps2/my-tableview
               {:data (filter-table filt selection search-text data),
                :page page,
                :page-size page-size,
                :selection selection})]
             (clojure.core/with-meta
              elem__31938__auto__
              '#:com.phronemophobic.membrane.schematic3{:ast
                                                        #:element{:id
                                                                  #uuid "daf846f4-130d-4155-a98b-a4a8ebe9e1df"}}))])]
         (clojure.core/with-meta
          elem__31938__auto__
          '#:com.phronemophobic.membrane.schematic3{:ast
                                                    #:element{:id
                                                              #uuid "d356c37b-32e1-47f8-91f0-f12bb104a311"}})))]
      (clojure.core/with-meta
       elem__31938__auto__
       '#:com.phronemophobic.membrane.schematic3{:ast
                                                 #:element{:id
                                                           #uuid "ce58732f-a9da-4291-a5cc-5562fcccf236"}}))
     (clojure.core/when-let
      [elem__31938__auto__
       (membrane.ui/translate
        9.33984375
        46.90234375
        (clojure.core/when-let
         [elem__31938__auto__
          (membrane.basic-components/dropdown
           {:options
            [[:all "All"] [:installed "Installed"] [:change "Change"]],
            :selected filt})]
         (clojure.core/with-meta
          elem__31938__auto__
          '#:com.phronemophobic.membrane.schematic3{:ast
                                                    #:element{:id
                                                              #uuid "248f0a83-ac25-46d6-bb25-5a1e7991d0c5"}})))]
      (clojure.core/with-meta
       elem__31938__auto__
       '#:com.phronemophobic.membrane.schematic3{:ast
                                                 #:element{:id
                                                           #uuid "b8beeed1-e2d9-438d-b8f4-630a2ced364d"}}))
     (clojure.core/when-let
      [elem__31938__auto__
       (membrane.ui/translate
        9.8671875
        14.1015625
        (clojure.core/when-let
         [elem__31938__auto__
          (membrane.basic-components/textarea {:text search-text})]
         (clojure.core/with-meta
          elem__31938__auto__
          '#:com.phronemophobic.membrane.schematic3{:ast
                                                    #:element{:id
                                                              #uuid "4e4c684e-6e66-418f-8f93-460a0938f770"}})))]
      (clojure.core/with-meta
       elem__31938__auto__
       '#:com.phronemophobic.membrane.schematic3{:ast
                                                 #:element{:id
                                                           #uuid "a3e0b183-486a-4373-a70a-85ed1e6e2a7e"}}))]]
   (clojure.core/with-meta
    elem__31938__auto__
    '#:com.phronemophobic.membrane.schematic3{:ast
                                              #:element{:id
                                                        #uuid "9ca0d833-35a5-40a8-bbae-dede23196548"}}))))


(defn -main [opts]
  
  (def app-state (atom (initial-state)))
  (add-watch app-state
             ::adjust-search
             (fn [k ref old new]
               (when (not= (:filt old)
                           (:filt new))
                 (swap! ref
                        (fn [state]
                          (-> state
                              (assoc :search-text "")
                              (update-page-info)))))))

  (add-watch app-state
             ::set-page
             (fn [k ref old new]
               (when (not= (:search-text old)
                           (:search-text new))
                 (swap! ref update-page-info))))

  (def app (component/make-app #'deps-app app-state))
  
  (let [[w h] (ui/bounds (app))]
    (backend/run-sync app
      {:window-start-width (+ w 4)
       :window-start-height (+ h 4)
       :window-title "Add Deps 2"}))
  nil)
