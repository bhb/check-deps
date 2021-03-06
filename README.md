# check-deps

Verify your `deps.edn` file

<img width="630" alt="1__tmux" src="https://user-images.githubusercontent.com/33398/46250550-34361500-c3fa-11e8-84cf-5f4625d6553c.png">

## Getting started

Add an alias to `~/.clojure/deps.edn`

```
;; within `:aliases` map
:check-deps {:extra-deps {bhb/check-deps
                           {:git/url "https://github.com/bhb/check-deps"
                            :sha "ce51e7e0814e4e6095027f34cd7a39475d95d153"}}
             :main-opts ["-m" "bhb.check-deps"]}}
```

## Usage

`cat deps.edn | clj -A:check-deps`

`echo "{:dep1 {}}" | clj -A:check-deps --no-color`

## License

Copyright © 2018  Ben Brinckerhoff

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
