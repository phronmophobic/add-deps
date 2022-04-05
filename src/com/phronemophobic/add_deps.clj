(ns com.phronemophobic.add-deps
  (:require [clojure.string :as str]
            [borkdude.rewrite-edn :as r]
            [membrane.ui :as ui]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
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
           (or version (first (:versions lib)))))


(defn read-edn-gz [url]
  (with-open [is (io/input-stream (io/as-url url))
              gz (GZIPInputStream. is)
              rdr (io/reader gz)
              rdr (PushbackReader. rdr)]
    (edn/read rdr)))



;;
;; AUTO GENERATED
;;

(def
  releases-url
  "https://api.github.com/repos/phronmophobic/dewey/releases/latest")
(def release-info (json/read-str (slurp (io/as-url releases-url))))
(def
  release-url
  (->
   release-info
   (get "assets")
   (->>
    (filter
     (fn*
      [p1__47357#]
      (= "deps-libs.edn.gz" (get p1__47357# "name")))))
   first
   (get "browser_download_url")))
(def deps-libs (read-edn-gz release-url))
(def star-factor 70)
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
     [p1__47364#]
     (assoc
      p1__47364#
      :downloads
      (or
       (downloads [(:group-id p1__47364#) (:artifact-id p1__47364#)])
       0))))
   (map
    (fn
      [m]
      (assoc
       m
       :versions
       (map
        (fn* [p1__47365#] (do #:mvn{:version p1__47365#}))
        (:versions m)))))))
(def n 54)
(def lib (nth libs n))
(def description (first (str/split-lines (:description lib ""))))
(def dlib (-> deps-libs first val))
(def
  normalized-dlib
  (merge
   dlib
   {:group-id (-> dlib :lib namespace),
    :artifact-id (-> dlib :lib name),
    :description (or (:description dlib) "")}))
(def
  dlib->lib
  (clojure.core/fn
    [dlib]
    (clojure.core/let
        [normalized-dlib
         (merge
          dlib
          {:group-id (-> dlib :lib namespace),
           :artifact-id (-> dlib :lib name),
           :description (or (:description dlib) "")})]
      normalized-dlib)))
(def clojar-git-urls (->> libs (map :scm) (map :url) (into #{})))
(def
  git-only
  (->>
   deps-libs
   vals
   (remove (fn* [p1__47370#] (clojar-git-urls (:url p1__47370#))))
   (filter (fn* [p1__47371#] (seq (:versions p1__47371#))))))
(def normalized-dep-libs (map dlib->lib git-only))
(def libs (concat libs normalized-dep-libs))
(def
  G__502882
  (->>
   normalized-dep-libs
   (filter
    (fn* [p1__47374#] (not (string? (:description p1__47374#)))))))
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
  (map
   (fn*
    [p1__47377#]
    (vector
     p1__47377#
     (or (:mvn/version p1__47377#) (:git/tag p1__47377#))))
   versions))
(def
  lib-viewed
  [(membrane.ui/translate
    6
    32
    (clojure.core/let
        [body-47383
         nil
         [width-47379 height-47380]
         (membrane.ui/bounds body-47383)]
      [(clojure.core/let
           [fill-body-47381
            (membrane.ui/label
             (:group-id lib)
             (clojure.core/let
                 [G__47382 (membrane.ui/font nil 16) G__47382 G__47382]
               G__47382))]
         (membrane.ui/with-style
           :membrane.ui/style-fill
           [(membrane.ui/with-color [0 0 0 0.7] fill-body-47381)]))
       nil
       body-47383]))
   (membrane.ui/translate
    5
    2
    (clojure.core/let
        [body-47388
         nil
         [width-47384 height-47385]
         (membrane.ui/bounds body-47388)]
      [(clojure.core/let
           [fill-body-47386
            (membrane.ui/label
             (:artifact-id lib)
             (clojure.core/let
                 [G__47387
                  (membrane.ui/font nil 24)
                  G__47387
                  (clojure.core/assoc G__47387 :weight :bold)]
               G__47387))]
         (membrane.ui/with-style
           :membrane.ui/style-fill
           [(membrane.ui/with-color [0 0 0 1] fill-body-47386)]))
       nil
       body-47388]))
   (membrane.ui/translate
    5
    54
    (clojure.core/let
        [body-47393
         nil
         [width-47389 height-47390]
         (membrane.ui/bounds body-47393)]
      [(clojure.core/let
           [fill-body-47391
            (membrane.ui/label
             (truncate description 80)
             (clojure.core/let
                 [G__47392 (membrane.ui/font nil 18) G__47392 G__47392]
               G__47392))]
         (membrane.ui/with-style
           :membrane.ui/style-fill
           [(membrane.ui/with-color [0 0 0 1] fill-body-47391)]))
       nil
       body-47393]))
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
         [body-47398
          nil
          [width-47394 height-47395]
          (membrane.ui/bounds body-47398)]
       [(clojure.core/let
            [fill-body-47396
             (membrane.ui/label
              the-lib-url
              (clojure.core/let
                  [G__47397 (membrane.ui/font nil 14) G__47397 G__47397]
                G__47397))]
          (membrane.ui/with-style
            :membrane.ui/style-fill
            [(membrane.ui/with-color [0 0 0 0.7] fill-body-47396)]))
        nil
        body-47398])))
   (membrane.ui/translate
    609
    75
    (basic/dropdown {:options version-options, :selected version}))
   (membrane.ui/translate
    553
    84
    (clojure.core/let
        [body-47403
         nil
         [width-47399 height-47400]
         (membrane.ui/bounds body-47403)]
      [(clojure.core/let
           [fill-body-47401
            (membrane.ui/label
             "version:"
             (clojure.core/let
                 [G__47402 (membrane.ui/font nil 14) G__47402 G__47402]
               G__47402))]
         (membrane.ui/with-style
           :membrane.ui/style-fill
           [(membrane.ui/with-color [0 0 0 0.7] fill-body-47401)]))
       nil
       body-47403]))
   (membrane.ui/translate
    4
    98
    (clojure.core/apply
     membrane.ui/horizontal-layout
     (clojure.core/interpose
      (membrane.ui/spacer 12 0)
      [(membrane.ui/translate
        0
        0
        (clojure.core/let
            [body-47408
             nil
             [width-47404 height-47405]
             (membrane.ui/bounds body-47408)]
          [(clojure.core/let
               [fill-body-47406
                (membrane.ui/label
                 (format "%,d downloads" (:downloads lib 0))
                 (clojure.core/let
                     [G__47407 (membrane.ui/font nil 14) G__47407 G__47407]
                   G__47407))]
             (membrane.ui/with-style
               :membrane.ui/style-fill
               [(membrane.ui/with-color [0 0 0 0.7] fill-body-47406)]))
           nil
           body-47408]))
       (membrane.ui/translate
        0
        0
        (clojure.core/let
            [body-47413
             nil
             [width-47409 height-47410]
             (membrane.ui/bounds body-47413)]
          [(clojure.core/let
               [fill-body-47411
                (membrane.ui/label
                 (str (:stars lib 0) " stars")
                 (clojure.core/let
                     [G__47412 (membrane.ui/font nil 14) G__47412 G__47412]
                   G__47412))]
             (membrane.ui/with-style
               :membrane.ui/style-fill
               [(membrane.ui/with-color [0 0 0 0.7] fill-body-47411)]))
           nil
           body-47413]))])))])
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
       (map
        (fn*
         [p1__47377#]
         (vector
          p1__47377#
          (or (:mvn/version p1__47377#) (:git/tag p1__47377#))))
        versions)
       lib-viewed
       [(membrane.ui/translate
         6
         32
         (clojure.core/let
             [body-47383
              nil
              [width-47379 height-47380]
              (membrane.ui/bounds body-47383)]
           [(clojure.core/let
                [fill-body-47381
                 (membrane.ui/label
                  (:group-id lib)
                  (clojure.core/let
                      [G__47382 (membrane.ui/font nil 16) G__47382 G__47382]
                    G__47382))]
              (membrane.ui/with-style
                :membrane.ui/style-fill
                [(membrane.ui/with-color [0 0 0 0.7] fill-body-47381)]))
            nil
            body-47383]))
        (membrane.ui/translate
         5
         2
         (clojure.core/let
             [body-47388
              nil
              [width-47384 height-47385]
              (membrane.ui/bounds body-47388)]
           [(clojure.core/let
                [fill-body-47386
                 (membrane.ui/label
                  (:artifact-id lib)
                  (clojure.core/let
                      [G__47387
                       (membrane.ui/font nil 24)
                       G__47387
                       (clojure.core/assoc G__47387 :weight :bold)]
                    G__47387))]
              (membrane.ui/with-style
                :membrane.ui/style-fill
                [(membrane.ui/with-color [0 0 0 1] fill-body-47386)]))
            nil
            body-47388]))
        (membrane.ui/translate
         5
         54
         (clojure.core/let
             [body-47393
              nil
              [width-47389 height-47390]
              (membrane.ui/bounds body-47393)]
           [(clojure.core/let
                [fill-body-47391
                 (membrane.ui/label
                  (truncate description 80)
                  (clojure.core/let
                      [G__47392 (membrane.ui/font nil 18) G__47392 G__47392]
                    G__47392))]
              (membrane.ui/with-style
                :membrane.ui/style-fill
                [(membrane.ui/with-color [0 0 0 1] fill-body-47391)]))
            nil
            body-47393]))
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
              [body-47398
               nil
               [width-47394 height-47395]
               (membrane.ui/bounds body-47398)]
            [(clojure.core/let
                 [fill-body-47396
                  (membrane.ui/label
                   the-lib-url
                   (clojure.core/let
                       [G__47397 (membrane.ui/font nil 14) G__47397 G__47397]
                     G__47397))]
               (membrane.ui/with-style
                 :membrane.ui/style-fill
                 [(membrane.ui/with-color [0 0 0 0.7] fill-body-47396)]))
             nil
             body-47398])))
        (membrane.ui/translate
         609
         75
         (basic/dropdown {:options version-options, :selected version}))
        (membrane.ui/translate
         553
         84
         (clojure.core/let
             [body-47403
              nil
              [width-47399 height-47400]
              (membrane.ui/bounds body-47403)]
           [(clojure.core/let
                [fill-body-47401
                 (membrane.ui/label
                  "version:"
                  (clojure.core/let
                      [G__47402 (membrane.ui/font nil 14) G__47402 G__47402]
                    G__47402))]
              (membrane.ui/with-style
                :membrane.ui/style-fill
                [(membrane.ui/with-color [0 0 0 0.7] fill-body-47401)]))
            nil
            body-47403]))
        (membrane.ui/translate
         4
         98
         (clojure.core/apply
          membrane.ui/horizontal-layout
          (clojure.core/interpose
           (membrane.ui/spacer 12 0)
           [(membrane.ui/translate
             0
             0
             (clojure.core/let
                 [body-47408
                  nil
                  [width-47404 height-47405]
                  (membrane.ui/bounds body-47408)]
               [(clojure.core/let
                    [fill-body-47406
                     (membrane.ui/label
                      (format "%,d downloads" (:downloads lib 0))
                      (clojure.core/let
                          [G__47407 (membrane.ui/font nil 14) G__47407 G__47407]
                        G__47407))]
                  (membrane.ui/with-style
                    :membrane.ui/style-fill
                    [(membrane.ui/with-color [0 0 0 0.7] fill-body-47406)]))
                nil
                body-47408]))
            (membrane.ui/translate
             0
             0
             (clojure.core/let
                 [body-47413
                  nil
                  [width-47409 height-47410]
                  (membrane.ui/bounds body-47413)]
               [(clojure.core/let
                    [fill-body-47411
                     (membrane.ui/label
                      (str (:stars lib 0) " stars")
                      (clojure.core/let
                          [G__47412 (membrane.ui/font nil 14) G__47412 G__47412]
                        G__47412))]
                  (membrane.ui/with-style
                    :membrane.ui/style-fill
                    [(membrane.ui/with-color [0 0 0 0.7] fill-body-47411)]))
                nil
                body-47413]))])))]]
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
             [p1__47573#]
             (min (dec num-pages) (inc p1__47573#)))]]))}))
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
            (fn* [p1__47574#] (max 0 (dec p1__47574#)))]]))}))
   (membrane.ui/translate
    55
    4
    (clojure.core/let
        [body-47579
         nil
         [width-47575 height-47576]
         (membrane.ui/bounds body-47579)]
      [(clojure.core/let
           [fill-body-47577
            (membrane.ui/label
             (str (format "%,d" (inc page)) "/" (format "%,d" num-pages))
             (clojure.core/let
                 [G__47578 (membrane.ui/font nil 14) G__47578 G__47578]
               G__47578))]
         (membrane.ui/with-style
           :membrane.ui/style-fill
           [(membrane.ui/with-color [0 0 0 1] fill-body-47577)]))
       nil
       body-47579]))])
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
                  [p1__47573#]
                  (min (dec num-pages) (inc p1__47573#)))]]))}))
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
                 (fn* [p1__47574#] (max 0 (dec p1__47574#)))]]))}))
        (membrane.ui/translate
         55
         4
         (clojure.core/let
             [body-47579
              nil
              [width-47575 height-47576]
              (membrane.ui/bounds body-47579)]
           [(clojure.core/let
                [fill-body-47577
                 (membrane.ui/label
                  (str
                   (format "%,d" (inc page))
                   "/"
                   (format "%,d" num-pages))
                  (clojure.core/let
                      [G__47578 (membrane.ui/font nil 14) G__47578 G__47578]
                    G__47578))]
              (membrane.ui/with-style
                :membrane.ui/style-fill
                [(membrane.ui/with-color [0 0 0 1] fill-body-47577)]))
            nil
            body-47579]))]]
    page-viewed))
(def G__95216 lib)
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
   (filter (fn* [p1__47706#] (matches? p1__47706# search-text)))))
(def result-num-pages (int (Math/ceil (/ (count matches) page-size))))
(def
  resulted
  (->>
   matches
   (sort-by
    (fn [m] (max (:downloads m 0) (* star-factor (:stars m 0)))))
   (reverse)
   (drop (* page-size result-page))
   (take page-size)))
(def
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
              [body-47712
               (membrane.ui/padding
                8
                8
                8
                8
                (lib-view
                 {:lib lib, :private (get private [:private lib])}))
               [width-47709 height-47710]
               (membrane.ui/bounds body-47712)]
            [nil
             (clojure.core/let
                 [stroke-body-47711
                  (membrane.ui/rectangle width-47709 height-47710)
                  stroke-body-47711
                  stroke-body-47711]
               (membrane.ui/with-style
                 :membrane.ui/style-stroke
                 [(membrane.ui/with-color [0 0 0 0.8] stroke-body-47711)]))
             body-47712]))]))))])
(membrane.component/defui
  libs-search
  [{:keys [libs search-text private]}]
  (clojure.core/let
      [star-factor
       70
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
           matched?))
       result-page
       (get private [:page search-text] 0)
       page-size
       5
       matches
       (->>
        libs
        (filter (fn* [p1__47706#] (matches? p1__47706# search-text))))
       result-num-pages
       (int (Math/ceil (/ (count matches) page-size)))
       resulted
       (->>
        matches
        (sort-by
         (fn [m] (max (:downloads m 0) (* star-factor (:stars m 0)))))
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
                   [body-47712
                    (membrane.ui/padding
                     8
                     8
                     8
                     8
                     (lib-view
                      {:lib lib, :private (get private [:private lib])}))
                    [width-47709 height-47710]
                    (membrane.ui/bounds body-47712)]
                 [nil
                  (clojure.core/let
                      [stroke-body-47711
                       (membrane.ui/rectangle width-47709 height-47710)
                       stroke-body-47711
                       stroke-body-47711]
                    (membrane.ui/with-style
                      :membrane.ui/style-stroke
                      [(membrane.ui/with-color
                         [0 0 0 0.8]
                         stroke-body-47711)]))
                  body-47712]))]))))]]
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
                      :window-start-height 808})
   

))


(comment
  (backend/save-image "add-deps.png" ((membrane.component/make-app #'libs-search {:libs libs :search-text "graph"})) )
  ,)
