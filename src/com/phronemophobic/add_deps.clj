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






(defn -main
  ([]
   (-main {}))
  ([opts]
;;
;; AUTO GENERATED
;;


   (clojure.core/let
       [releases-url
     "https://api.github.com/repos/phronmophobic/dewey/releases/latest"
     release-info
     (json/read-str (slurp (io/as-url releases-url)))
     release-url
     (->
      release-info
      (get "assets")
      (->>
       (filter
        (fn*
         [p1__47380#]
         (= "deps-libs.edn.gz" (get p1__47380# "name")))))
      first
      (get "browser_download_url"))
     deps-libs
     (read-edn-gz release-url)
     star-factor
     70
     stats
     (edn/read-string
      (slurp (io/as-url "https://repo.clojars.org/stats/all.edn")))
     downloads
     (->>
      stats
      (into {} (map (fn [[lib stats]] [lib (reduce + (vals stats))]))))
     feed-str
     (with-open
       [is
        (io/input-stream
         (io/as-url "https://clojars.org/repo/feed.clj.gz"))
        gz
        (gzip-stream is)]
       (slurp gz))
     libs
     (->>
      feed-str
      clojure.string/split-lines
      (map edn/read-string)
      (map
       (fn*
        [p1__763267#]
        (assoc
         p1__763267#
         :downloads
         (or
          (downloads [(:group-id p1__763267#) (:artifact-id p1__763267#)])
          0))))
      (map
       (fn
         [m]
         (assoc
          m
          :versions
          (map
           (fn* [p1__763268#] (do #:mvn{:version p1__763268#}))
           (:versions m))))))
     n
     54
     lib
     (nth libs n)
     description
     (first (str/split-lines (:description lib "")))
     dlib
     (-> deps-libs first val)
     normalized-dlib
     (merge
      dlib
      {:group-id (-> dlib :lib namespace),
       :artifact-id (-> dlib :lib name),
       :description (or (:description dlib) "")})
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
           normalized-dlib))
     clojar-git-urls
     (->> libs (map :scm) (map :url) (into #{}))
     git-only
     (->>
      deps-libs
      vals
      (remove (fn* [p1__689142#] (clojar-git-urls (:url p1__689142#))))
      (filter (fn* [p1__689143#] (seq (:versions p1__689143#)))))
     normalized-dep-libs
     (map dlib->lib git-only)
     libs
     (concat libs normalized-dep-libs)
     G__502882
     (->>
      normalized-dep-libs
      (filter
       (fn* [p1__548179#] (not (string? (:description p1__548179#))))))
     lib-url
     (:url lib)
     the-lib-url
     (when (not= "http://example.com/FIXME" lib-url) lib-url)
     truncate
     (fn [s n] (subs s 0 (min n (count s))))
     versions
     (:versions lib)
     private
     {}
     version
     (get private :version (first versions))
     version-options
     (map
      (fn*
       [p1__775460#]
       (vector
        p1__775460#
        (or (:mvn/version p1__775460#) (:git/tag p1__775460#))))
      versions)
     lib-viewed
     [(membrane.ui/translate
       6
       32
       (clojure.core/let
           [body-47396
            nil
            [width-47392 height-47393]
            (membrane.ui/bounds body-47396)]
           [(clojure.core/let
                [fill-body-47394
                 (membrane.ui/label
                  (:group-id lib)
                  (clojure.core/let
                      [G__47395 (membrane.ui/font nil 16) G__47395 G__47395]
                      G__47395))]
                (membrane.ui/with-style
                  :membrane.ui/style-fill
                  [(membrane.ui/with-color [0 0 0 0.7] fill-body-47394)]))
            nil
            body-47396]))
      (membrane.ui/translate
       5
       2
       (clojure.core/let
           [body-47401
            nil
            [width-47397 height-47398]
            (membrane.ui/bounds body-47401)]
           [(clojure.core/let
                [fill-body-47399
                 (membrane.ui/label
                  (:artifact-id lib)
                  (clojure.core/let
                      [G__47400
                       (membrane.ui/font nil 24)
                       G__47400
                       (clojure.core/assoc G__47400 :weight :bold)]
                      G__47400))]
                (membrane.ui/with-style
                  :membrane.ui/style-fill
                  [(membrane.ui/with-color [0 0 0 1] fill-body-47399)]))
            nil
            body-47401]))
      (membrane.ui/translate
       5
       54
       (clojure.core/let
           [body-47411
            nil
            [width-47407 height-47408]
            (membrane.ui/bounds body-47411)]
           [(clojure.core/let
                [fill-body-47409
                 (membrane.ui/label
                  (truncate description 80)
                  (clojure.core/let
                      [G__47410 (membrane.ui/font nil 18) G__47410 G__47410]
                      G__47410))]
                (membrane.ui/with-style
                  :membrane.ui/style-fill
                  [(membrane.ui/with-color [0 0 0 1] fill-body-47409)]))
            nil
            body-47411]))
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
            [body-47416
             nil
             [width-47412 height-47413]
             (membrane.ui/bounds body-47416)]
            [(clojure.core/let
                 [fill-body-47414
                  (membrane.ui/label
                   the-lib-url
                   (clojure.core/let
                       [G__47415 (membrane.ui/font nil 14) G__47415 G__47415]
                       G__47415))]
                 (membrane.ui/with-style
                   :membrane.ui/style-fill
                   [(membrane.ui/with-color [0 0 0 0.7] fill-body-47414)]))
             nil
             body-47416])))
      (membrane.ui/translate
       609
       75
       (basic/dropdown {:options version-options, :selected version}))
      (membrane.ui/translate
       553
       84
       (clojure.core/let
           [body-47421
            nil
            [width-47417 height-47418]
            (membrane.ui/bounds body-47421)]
           [(clojure.core/let
                [fill-body-47419
                 (membrane.ui/label
                  "version:"
                  (clojure.core/let
                      [G__47420 (membrane.ui/font nil 14) G__47420 G__47420]
                      G__47420))]
                (membrane.ui/with-style
                  :membrane.ui/style-fill
                  [(membrane.ui/with-color [0 0 0 0.7] fill-body-47419)]))
            nil
            body-47421]))
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
               [body-675568
                nil
                [width-675564 height-675565]
                (membrane.ui/bounds body-675568)]
               [(clojure.core/let
                    [fill-body-675566
                     (membrane.ui/label
                      (format "%,d downloads" (:downloads lib 0))
                      (clojure.core/let
                          [G__675567 (membrane.ui/font nil 14) G__675567 G__675567]
                          G__675567))]
                    (membrane.ui/with-style
                      :membrane.ui/style-fill
                      [(membrane.ui/with-color [0 0 0 0.7] fill-body-675566)]))
                nil
                body-675568]))
          (membrane.ui/translate
           0
           0
           (clojure.core/let
               [body-675573
                nil
                [width-675569 height-675570]
                (membrane.ui/bounds body-675573)]
               [(clojure.core/let
                    [fill-body-675571
                     (membrane.ui/label
                      (str (:stars lib 0) " stars")
                      (clojure.core/let
                          [G__675572 (membrane.ui/font nil 14) G__675572 G__675572]
                          G__675572))]
                    (membrane.ui/with-style
                      :membrane.ui/style-fill
                      [(membrane.ui/with-color [0 0 0 0.7] fill-body-675571)]))
                nil
                body-675573]))])))]
     lib-view
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
              [p1__775460#]
              (vector
               p1__775460#
               (or (:mvn/version p1__775460#) (:git/tag p1__775460#))))
             versions)
            lib-viewed
            [(membrane.ui/translate
              6
              32
              (clojure.core/let
                  [body-47396
                   nil
                   [width-47392 height-47393]
                   (membrane.ui/bounds body-47396)]
                  [(clojure.core/let
                       [fill-body-47394
                        (membrane.ui/label
                         (:group-id lib)
                         (clojure.core/let
                             [G__47395 (membrane.ui/font nil 16) G__47395 G__47395]
                             G__47395))]
                       (membrane.ui/with-style
                         :membrane.ui/style-fill
                         [(membrane.ui/with-color [0 0 0 0.7] fill-body-47394)]))
                   nil
                   body-47396]))
             (membrane.ui/translate
              5
              2
              (clojure.core/let
                  [body-47401
                   nil
                   [width-47397 height-47398]
                   (membrane.ui/bounds body-47401)]
                  [(clojure.core/let
                       [fill-body-47399
                        (membrane.ui/label
                         (:artifact-id lib)
                         (clojure.core/let
                             [G__47400
                              (membrane.ui/font nil 24)
                              G__47400
                              (clojure.core/assoc G__47400 :weight :bold)]
                             G__47400))]
                       (membrane.ui/with-style
                         :membrane.ui/style-fill
                         [(membrane.ui/with-color [0 0 0 1] fill-body-47399)]))
                   nil
                   body-47401]))
             (membrane.ui/translate
              5
              54
              (clojure.core/let
                  [body-47411
                   nil
                   [width-47407 height-47408]
                   (membrane.ui/bounds body-47411)]
                  [(clojure.core/let
                       [fill-body-47409
                        (membrane.ui/label
                         (truncate description 80)
                         (clojure.core/let
                             [G__47410 (membrane.ui/font nil 18) G__47410 G__47410]
                             G__47410))]
                       (membrane.ui/with-style
                         :membrane.ui/style-fill
                         [(membrane.ui/with-color [0 0 0 1] fill-body-47409)]))
                   nil
                   body-47411]))
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
                   [body-47416
                    nil
                    [width-47412 height-47413]
                    (membrane.ui/bounds body-47416)]
                   [(clojure.core/let
                        [fill-body-47414
                         (membrane.ui/label
                          the-lib-url
                          (clojure.core/let
                              [G__47415 (membrane.ui/font nil 14) G__47415 G__47415]
                              G__47415))]
                        (membrane.ui/with-style
                          :membrane.ui/style-fill
                          [(membrane.ui/with-color [0 0 0 0.7] fill-body-47414)]))
                    nil
                    body-47416])))
             (membrane.ui/translate
              609
              75
              (basic/dropdown {:options version-options, :selected version}))
             (membrane.ui/translate
              553
              84
              (clojure.core/let
                  [body-47421
                   nil
                   [width-47417 height-47418]
                   (membrane.ui/bounds body-47421)]
                  [(clojure.core/let
                       [fill-body-47419
                        (membrane.ui/label
                         "version:"
                         (clojure.core/let
                             [G__47420 (membrane.ui/font nil 14) G__47420 G__47420]
                             G__47420))]
                       (membrane.ui/with-style
                         :membrane.ui/style-fill
                         [(membrane.ui/with-color [0 0 0 0.7] fill-body-47419)]))
                   nil
                   body-47421]))
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
                      [body-675568
                       nil
                       [width-675564 height-675565]
                       (membrane.ui/bounds body-675568)]
                      [(clojure.core/let
                           [fill-body-675566
                            (membrane.ui/label
                             (format "%,d downloads" (:downloads lib 0))
                             (clojure.core/let
                                 [G__675567
                                  (membrane.ui/font nil 14)
                                  G__675567
                                  G__675567]
                                 G__675567))]
                           (membrane.ui/with-style
                             :membrane.ui/style-fill
                             [(membrane.ui/with-color
                                [0 0 0 0.7]
                                fill-body-675566)]))
                       nil
                       body-675568]))
                 (membrane.ui/translate
                  0
                  0
                  (clojure.core/let
                      [body-675573
                       nil
                       [width-675569 height-675570]
                       (membrane.ui/bounds body-675573)]
                      [(clojure.core/let
                           [fill-body-675571
                            (membrane.ui/label
                             (str (:stars lib 0) " stars")
                             (clojure.core/let
                                 [G__675572
                                  (membrane.ui/font nil 14)
                                  G__675572
                                  G__675572]
                                 G__675572))]
                           (membrane.ui/with-style
                             :membrane.ui/style-fill
                             [(membrane.ui/with-color
                                [0 0 0 0.7]
                                fill-body-675571)]))
                       nil
                       body-675573]))])))]]
           lib-viewed))
     page
     0
     num-pages
     6
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
                [p1__47575#]
                (min (dec num-pages) (inc p1__47575#)))]]))}))
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
               (fn* [p1__47576#] (max 0 (dec p1__47576#)))]]))}))
      (membrane.ui/translate
       55
       4
       (clojure.core/let
           [body-47581
            nil
            [width-47577 height-47578]
            (membrane.ui/bounds body-47581)]
           [(clojure.core/let
                [fill-body-47579
                 (membrane.ui/label
                  (str (format "%,d" (inc page)) "/" (format "%,d" num-pages))
                  (clojure.core/let
                      [G__47580 (membrane.ui/font nil 14) G__47580 G__47580]
                      G__47580))]
                (membrane.ui/with-style
                  :membrane.ui/style-fill
                  [(membrane.ui/with-color [0 0 0 1] fill-body-47579)]))
            nil
            body-47581]))]
     page-view
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
                       [p1__47575#]
                       (min (dec num-pages) (inc p1__47575#)))]]))}))
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
                      (fn* [p1__47576#] (max 0 (dec p1__47576#)))]]))}))
             (membrane.ui/translate
              55
              4
              (clojure.core/let
                  [body-47581
                   nil
                   [width-47577 height-47578]
                   (membrane.ui/bounds body-47581)]
                  [(clojure.core/let
                       [fill-body-47579
                        (membrane.ui/label
                         (str
                          (format "%,d" (inc page))
                          "/"
                          (format "%,d" num-pages))
                         (clojure.core/let
                             [G__47580 (membrane.ui/font nil 14) G__47580 G__47580]
                             G__47580))]
                       (membrane.ui/with-style
                         :membrane.ui/style-fill
                         [(membrane.ui/with-color [0 0 0 1] fill-body-47579)]))
                   nil
                   body-47581]))]]
           page-viewed))
     G__95216
     lib
     search-text
     "sci"
     search-text-lower
     (str/lower-case search-text)
     ks
     [:artifact-id :description]
     lib-description
     (->> (get lib :description "") str/split-lines first str/lower-case)
     matched?
     (or
      (str/includes?
       (str/lower-case (get lib :artifact-id))
       search-text-lower)
      (str/includes? lib-description search-text-lower))
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
     private
     {}
     result-page
     (get private [:page search-text] 0)
     page-size
     5
     matches
     (->>
      libs
      (filter (fn* [p1__47708#] (matches? p1__47708# search-text))))
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
                   [body-47713
                    (membrane.ui/padding
                     8
                     8
                     8
                     8
                     (lib-view
                      {:lib lib, :private (get private [:private lib])}))
                    [width-47710 height-47711]
                    (membrane.ui/bounds body-47713)]
                   [nil
                    (clojure.core/let
                        [stroke-body-47712
                         (membrane.ui/rectangle width-47710 height-47711)
                         stroke-body-47712
                         stroke-body-47712]
                        (membrane.ui/with-style
                          :membrane.ui/style-stroke
                          [(membrane.ui/with-color [0 0 0 0.8] stroke-body-47712)]))
                    body-47713]))]))))]
     libs-search
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
             (filter (fn* [p1__47708#] (matches? p1__47708# search-text))))
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
                          [body-47713
                           (membrane.ui/padding
                            8
                            8
                            8
                            8
                            (lib-view
                             {:lib lib, :private (get private [:private lib])}))
                           [width-47710 height-47711]
                           (membrane.ui/bounds body-47713)]
                          [nil
                           (clojure.core/let
                               [stroke-body-47712
                                (membrane.ui/rectangle width-47710 height-47711)
                                stroke-body-47712
                                stroke-body-47712]
                               (membrane.ui/with-style
                                 :membrane.ui/style-stroke
                                 [(membrane.ui/with-color
                                    [0 0 0 0.8]
                                    stroke-body-47712)]))
                           body-47713]))]))))]]
         libs-searched))]


     ;;
;; END AUTO GENERATED
;;

   (backend/run-sync (membrane.component/make-app #'libs-search {:libs libs :search-text ""})
                     {:window-title "Add Deps"
                      :window-start-width 800
                      :window-start-height 808})
)

))


(comment
  (backend/save-image "add-deps.png" ((membrane.component/make-app #'libs-search {:libs libs :search-text "graph"})) )
  ,)
