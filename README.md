# check-deps

Verify your `deps.edn` file

## Getting started

Add an alias to `~/.clojure/deps.edn`

```
;; within `:aliases` map
:check-deps {:extra-deps {bhb/check-deps
                           {:git/url "https://github.com/bhb/check-deps"
                            :sha "894e2418e05e341e62efb00ccca0aae740ec0c6d"}}
             :main-opts ["-m" "bhb.check-deps"]}}
```

## Usage

`cat deps.edn | clj -A:check-deps`

## Options

FIXME

## License

Copyright © 2018  Ben Brinckerhoff

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
