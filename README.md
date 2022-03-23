# add-deps

Swing GUI application for adding clojars deps to deps.edn projects.

## Screenshot

![add-deps](/add-deps.png?raw=true)

## Usage

Create an alias for add-deps

```clojure

{
 :aliases {
```
```clojure
  :add-deps {:replace-deps
             {com.phronemophobic/add-deps {:mvn/version "1.0"}}
             :exec-fn com.phronemophobic.add-deps/-main}
```
```clojure
           }
}
```

Run add deps:

```sh
$ clojure -X:add-deps
```

## License

Copyright © 2022 Adrian

Distributed under the Eclipse Public License version 1.0.
