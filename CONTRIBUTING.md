# Contributing

Railway Network Simulator welcomes focused improvements that preserve the clarity of its infrastructure model.

## Before opening a change

- Use a short-lived branch from `main`.
- Keep behavior changes separate from documentation-only changes.
- Discuss event grammar, lifecycle timing, or freight-semantic changes in an issue first.
- Do not commit IDE settings, generated graphs, logs, build output, or credentials.

## Local verification

```bash
./gradlew clean build
```

For presentation changes, also run the simulator and render its Graphviz output:

```bash
./gradlew run
dot -Tsvg build/outputs/railway-network.dot \
  -o build/outputs/railway-network.svg
```

Review both the console table and rendered graph before submitting.

## Commit style

Use an imperative Conventional Commit subject:

```text
feat: add scheduled maintenance state
fix: preserve freight during route upgrade
docs: explain alternating track direction
test: cover duplicate reverse route
```

## Pull requests

A strong pull request:

- explains the problem and chosen boundary;
- includes tests that fail without the change;
- updates event, lifecycle, or freight documentation when needed;
- states compatibility and Graphviz-output risks;
- avoids unrelated formatting or generated files.
