# Security policy

## Scope

V-Watcher is an Android application focused on local device inspection. Please do
not include personal data, API keys, signing credentials, or private device logs in
an issue or pull request.

## Reporting a vulnerability

Do not report security vulnerabilities in a public issue. Use GitHub's private
security advisory flow for this repository, or contact the repository maintainers
privately with:

- a concise description of the issue;
- affected version or commit;
- reproduction steps or proof of concept;
- impact and suggested mitigation, when known.

Allow maintainers reasonable time to investigate before public disclosure.

## Secret handling

Secrets must be supplied through the local environment or an approved secret
manager. They must not be stored in source files, `.env` files, Git configuration,
build artifacts, or documentation. If a credential is exposed, revoke it
immediately and replace it.
