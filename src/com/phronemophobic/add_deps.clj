(ns com.phronemophobic.add-deps
  (:require [clojure.string :as str]
            [borkdude.rewrite-edn :as r]
            [membrane.ui :as ui]
            [clojure.java.io :as io]
            [membrane.basic-components :as basic]
            [membrane.component :refer [defeffect] ]
            [membrane.java2d :as backend]
            [clojure.java.shell :as sh]
            [clojure.edn :as edn])
  (:import java.io.PushbackReader
           java.util.zip.GZIPInputStream)
  (:gen-class))


(defmacro with-refs [ref-names & body]
  `(let ~(into
            []
            cat
            (for [nm ref-names
                  :when (and (symbol? nm)
                             (not (contains? &env nm)))]
              [nm nil]))
       ~@body))

(defn gzip-stream [is]
  (GZIPInputStream. is))

(defeffect :open-url [url]
  (sh/sh "open" url))


(defn add-dep [dep coord]
  (let [edn-string (slurp "deps.edn")
        nodes (r/parse-string edn-string)]
    (spit "deps.edn" (str (r/assoc-in nodes [:deps dep] coord)))))


(defeffect :add-dep [lib version]
  (add-dep (symbol (name (:group-id lib))
                   (name (:artifact-id lib)))
           {:mvn/version (or version (first (:versions lib)))})
  )

;;
;; AUTO GENERATED
;;

(def
  stats
  (edn/read-string
   (slurp (io/as-url "https://repo.clojars.org/stats/all.edn"))))
(def
  downloads
  (->>
   stats
   (into {} (map (fn [[lib stats]] [lib (reduce + (vals stats))])))))
(def
  feed-str
  (with-open
    [is
     (io/input-stream
      (io/as-url "https://clojars.org/repo/feed.clj.gz"))
     gz
     (gzip-stream is)]
    (slurp gz)))
(def
  libs
  (->>
   feed-str
   clojure.string/split-lines
   (map edn/read-string)
   (map
    (fn*
     [p1__47126#]
     (assoc
      p1__47126#
      :downloads
      (or
       (downloads [(:group-id p1__47126#) (:artifact-id p1__47126#)])
       0))))))
(def n 27)
(def lib (nth libs n))
(def description (first (str/split-lines (:description lib ""))))
(def lib-url (:url lib))
(def
  the-lib-url
  (when (not= "http://example.com/FIXME" lib-url) lib-url))
(def truncate (fn [s n] (subs s 0 (min n (count s)))))
(def versions (:versions lib))
(def private {})
(def version (get private :version (first versions)))
(def
  version-options
  (map (fn* [p1__47129#] (vector p1__47129# p1__47129#)) versions))
(membrane.component/defui
  lib-view
  [{:keys [lib private]}]
  (clojure.core/let
      [description
       (first (str/split-lines (:description lib "")))
       lib-url
       (:url lib)
       the-lib-url
       (when (not= "http://example.com/FIXME" lib-url) lib-url)
       truncate
       (fn [s n] (subs s 0 (min n (count s))))
       versions
       (:versions lib)
       version
       (get private :version (first versions))
       version-options
       (map (fn* [p1__47129#] (vector p1__47129# p1__47129#)) versions)
       lib-viewed
       [(membrane.ui/translate
         6
         32
         (clojure.core/let
             [body-47135
              nil
              [width-47131 height-47132]
              (membrane.ui/bounds body-47135)]
           [(clojure.core/let
                [fill-body-47133
                 (membrane.ui/label
                  (:group-id lib)
                  (clojure.core/let
                      [G__47134 (membrane.ui/font nil 16) G__47134 G__47134]
                    G__47134))]
              (membrane.ui/with-style
                :membrane.ui/style-fill
                [(membrane.ui/with-color [0 0 0 0.7] fill-body-47133)]))
            nil
            body-47135]))
        (membrane.ui/translate
         5
         2
         (clojure.core/let
             [body-47140
              nil
              [width-47136 height-47137]
              (membrane.ui/bounds body-47140)]
           [(clojure.core/let
                [fill-body-47138
                 (membrane.ui/label
                  (:artifact-id lib)
                  (clojure.core/let
                      [G__47139
                       (membrane.ui/font nil 24)
                       G__47139
                       (clojure.core/assoc G__47139 :weight :bold)]
                    G__47139))]
              (membrane.ui/with-style
                :membrane.ui/style-fill
                [(membrane.ui/with-color [0 0 0 1] fill-body-47138)]))
            nil
            body-47140]))
        (membrane.ui/translate
         4
         98
         (clojure.core/let
             [body-47145
              nil
              [width-47141 height-47142]
              (membrane.ui/bounds body-47145)]
           [(clojure.core/let
                [fill-body-47143
                 (membrane.ui/label
                  (format "%,d downloads" (:downloads lib))
                  (clojure.core/let
                      [G__47144 (membrane.ui/font nil 14) G__47144 G__47144]
                    G__47144))]
              (membrane.ui/with-style
                :membrane.ui/style-fill
                [(membrane.ui/with-color [0 0 0 0.7] fill-body-47143)]))
            nil
            body-47145]))
        (membrane.ui/translate
         5
         54
         (clojure.core/let
             [body-47150
              nil
              [width-47146 height-47147]
              (membrane.ui/bounds body-47150)]
           [(clojure.core/let
                [fill-body-47148
                 (membrane.ui/label
                  (truncate description 80)
                  (clojure.core/let
                      [G__47149 (membrane.ui/font nil 18) G__47149 G__47149]
                    G__47149))]
              (membrane.ui/with-style
                :membrane.ui/style-fill
                [(membrane.ui/with-color [0 0 0 1] fill-body-47148)]))
            nil
            body-47150]))
        (membrane.ui/translate
         694
         7
         (with-refs
           [extra]
           (basic/button
            {:text "add dep",
             :hover? (get extra [lib :hover?]),
             :on-click (fn [] [[:add-dep lib version]])})))
        (membrane.ui/translate
         4
         78
         (membrane.ui/on
          :mouse-down
          (fn [_] [[:open-url the-lib-url]])
          (clojure.core/let
              [body-47155
               nil
               [width-47151 height-47152]
               (membrane.ui/bounds body-47155)]
            [(clojure.core/let
                 [fill-body-47153
                  (membrane.ui/label
                   the-lib-url
                   (clojure.core/let
                       [G__47154 (membrane.ui/font nil 14) G__47154 G__47154]
                     G__47154))]
               (membrane.ui/with-style
                 :membrane.ui/style-fill
                 [(membrane.ui/with-color [0 0 0 0.7] fill-body-47153)]))
             nil
             body-47155])))
        (membrane.ui/translate
         609
         75
         (basic/dropdown {:options version-options, :selected version}))
        (membrane.ui/translate
         553
         84
         (clojure.core/let
             [body-47160
              nil
              [width-47156 height-47157]
              (membrane.ui/bounds body-47160)]
           [(clojure.core/let
                [fill-body-47158
                 (membrane.ui/label
                  "version:"
                  (clojure.core/let
                      [G__47159 (membrane.ui/font nil 14) G__47159 G__47159]
                    G__47159))]
              (membrane.ui/with-style
                :membrane.ui/style-fill
                [(membrane.ui/with-color [0 0 0 0.7] fill-body-47158)]))
            nil
            body-47160]))]]
    lib-viewed))
(def page 0)
(def num-pages 6)
(def
  page-viewed
  [(membrane.ui/translate
    26
    0
    (basic/button
     {:text ">",
      :on-click
      (fn
        []
        (with-refs
          [$page]
          [[:update
            $page
            (fn*
             [p1__47314#]
             (min (dec num-pages) (inc p1__47314#)))]]))}))
   (membrane.ui/translate
    0
    0
    (basic/button
     {:text "<",
      :on-click
      (fn
        []
        (with-refs
          [$page]
          [[:update
            $page
            (fn* [p1__47315#] (max 0 (dec p1__47315#)))]]))}))
   (membrane.ui/translate
    55
    4
    (clojure.core/let
        [body-47320
         nil
         [width-47316 height-47317]
         (membrane.ui/bounds body-47320)]
      [(clojure.core/let
           [fill-body-47318
            (membrane.ui/label
             (str (format "%,d" (inc page)) "/" (format "%,d" num-pages))
             (clojure.core/let
                 [G__47319 (membrane.ui/font nil 14) G__47319 G__47319]
               G__47319))]
         (membrane.ui/with-style
           :membrane.ui/style-fill
           [(membrane.ui/with-color [0 0 0 1] fill-body-47318)]))
       nil
       body-47320]))])
(membrane.component/defui
  page-view
  [{:keys [page num-pages]}]
  (clojure.core/let
      [page-viewed
       [(membrane.ui/translate
         26
         0
         (basic/button
          {:text ">",
           :on-click
           (fn
             []
             (with-refs
               [$page]
               [[:update
                 $page
                 (fn*
                  [p1__47314#]
                  (min (dec num-pages) (inc p1__47314#)))]]))}))
        (membrane.ui/translate
         0
         0
         (basic/button
          {:text "<",
           :on-click
           (fn
             []
             (with-refs
               [$page]
               [[:update
                 $page
                 (fn* [p1__47315#] (max 0 (dec p1__47315#)))]]))}))
        (membrane.ui/translate
         55
         4
         (clojure.core/let
             [body-47320
              nil
              [width-47316 height-47317]
              (membrane.ui/bounds body-47320)]
           [(clojure.core/let
                [fill-body-47318
                 (membrane.ui/label
                  (str
                   (format "%,d" (inc page))
                   "/"
                   (format "%,d" num-pages))
                  (clojure.core/let
                      [G__47319 (membrane.ui/font nil 14) G__47319 G__47319]
                    G__47319))]
              (membrane.ui/with-style
                :membrane.ui/style-fill
                [(membrane.ui/with-color [0 0 0 1] fill-body-47318)]))
            nil
            body-47320]))]]
    page-viewed))
(def search-text "sci")
(def search-text-lower (str/lower-case search-text))
(def ks [:artifact-id :description])
(def
  lib-description
  (->> (get lib :description "") str/split-lines first str/lower-case))
(def
  matched?
  (or
   (str/includes?
    (str/lower-case (get lib :artifact-id))
    search-text-lower)
   (str/includes? lib-description search-text-lower)))
(def
  matches?
  (clojure.core/fn
    [lib search-text]
    (clojure.core/let
        [search-text-lower
         (str/lower-case search-text)
         lib-description
         (->>
          (get lib :description "")
          str/split-lines
          first
          str/lower-case)
         matched?
         (or
          (str/includes?
           (str/lower-case (get lib :artifact-id))
           search-text-lower)
          (str/includes? lib-description search-text-lower))]
      matched?)))
(def private {})
(def result-page (get private [:page search-text] 0))
(def page-size 5)
(def
  matches
  (->>
   libs
   (filter (fn* [p1__47447#] (matches? p1__47447# search-text)))))
(def result-num-pages (int (Math/ceil (/ (count matches) page-size))))
(def
  resulted
  (->>
   matches
   (sort-by :downloads)
   (reverse)
   (drop (* page-size result-page))
   (take page-size)))
(membrane.component/defui
  libs-search
  [{:keys [libs search-text private]}]
  (clojure.core/let
      [matches?
       (clojure.core/fn
         [lib search-text]
         (clojure.core/let
             [search-text-lower
              (str/lower-case search-text)
              lib-description
              (->>
               (get lib :description "")
               str/split-lines
               first
               str/lower-case)
              matched?
              (or
               (str/includes?
                (str/lower-case (get lib :artifact-id))
                search-text-lower)
               (str/includes? lib-description search-text-lower))]
           matched?))
       result-page
       (get private [:page search-text] 0)
       page-size
       5
       matches
       (->>
        libs
        (filter (fn* [p1__47447#] (matches? p1__47447# search-text))))
       result-num-pages
       (int (Math/ceil (/ (count matches) page-size)))
       resulted
       (->>
        matches
        (sort-by :downloads)
        (reverse)
        (drop (* page-size result-page))
        (take page-size))
       libs-searched
       [(membrane.ui/translate 12 9 (basic/textarea {:text search-text}))
        (membrane.ui/translate
         14
         45
         (page-view {:page result-page, :num-pages result-num-pages}))
        (membrane.ui/translate
         11
         87
         (clojure.core/apply
          membrane.ui/vertical-layout
          (clojure.core/vec
           (clojure.core/for
               [lib resulted]
             [(membrane.ui/translate
               0
               0
               (clojure.core/let
                   [body-47452
                    (membrane.ui/padding
                     8
                     8
                     8
                     8
                     (lib-view
                      {:lib lib, :private (get private [:private lib])}))
                    [width-47449 height-47450]
                    (membrane.ui/bounds body-47452)]
                 [nil
                  (clojure.core/let
                      [stroke-body-47451
                       (membrane.ui/rectangle width-47449 height-47450)
                       stroke-body-47451
                       stroke-body-47451]
                    (membrane.ui/with-style
                      :membrane.ui/style-stroke
                      [(membrane.ui/with-color
                         [0 0 0 0.8]
                         stroke-body-47451)]))
                  body-47452]))]))))]]
    libs-searched))

;;
;; END AUTO GENERATED
;;



(defn -main
  ([]
   (-main {}))
  ([opts]
   (backend/run-sync (membrane.component/make-app #'libs-search {:libs libs :search-text ""})
                     {:window-title "Add Deps"
                      :window-start-width 800
                      :window-start-height 808})))


