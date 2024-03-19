let
  pkgs = import <nixpkgs> {};
in
  pkgs.mkShell {
    buildInputs = with pkgs; [
      lolcat
      clojure
      babashka
      clojure-lsp
      leiningen
      gum
      jdk
      python311Packages.epc
      python311Packages.orjson
      python311Packages.sexpdata
      python311Packages.six
      python311Packages.setuptools
      python311Packages.paramiko
      python311Packages.rapidfuzz
    ];
    shellHook = ''
    echo "initiating clojure env..." | lolcat
    '';
  }
