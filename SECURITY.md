# Security policy

## Supported version

Security fixes are applied to the latest published release and the `main` branch.

## Report privately

Please do not open a public issue for a suspected vulnerability.

Use GitHub’s **Security → Report a vulnerability** flow:

<https://github.com/Himath2002/railway-network-simulator/security/advisories/new>

Include:

- the affected version or commit;
- the smallest event sequence or reproduction;
- expected security impact;
- operating system and JDK version;
- any suggested mitigation.

Reports will be acknowledged through the private advisory. Details should remain private until a fix and coordinated disclosure are ready.

## Security posture

The application runs locally, performs no network requests, stores no credentials, and collects no telemetry. Its primary untrusted boundaries are event text, console input, and the configured Graphviz output path.

Event structure, numeric ranges, entity references, uniqueness, route endpoints, and lifecycle eligibility are validated before network mutation. DOT identifiers are escaped before UTF-8 output. Generated graphs, logs, build output, and local environment files are excluded from version control.
