[![Build Status](https://travis-ci.org/bloomreach-forge/document-commenting.svg?branch=develop)](https://travis-ci.org/bloomreach-forge/document-commenting)

# Bloomreach XM Document Commenting Plugin

This project provides commenting functionality for content authors and editors in CMS authoring application.

# Documentation (Local)

The documentation can generated locally by this command:

```bash
$ mvn clean install
$ mvn clean site
```

The output is in the ```target/site/``` directory by default. You can open ```target/site/index.html``` in a browser.

# Documentation (GitHub Pages)

Documentation is available at [https://bloomreach-forge.github.io/document-commenting/](https://bloomreach-forge.github.io/document-commenting/).

You can generate the GitHub pages only from ```master``` branch by this command:

```bash
$ mvn clean install
$ find docs -name "*.html" -exec rm {} \;
$ mvn -Pgithub.pages clean site
```

The output is in the ```docs/``` directory by default. You can open ```docs/index.html``` in a browser.

You can push it and GitHub Pages will be served for the site automatically.
