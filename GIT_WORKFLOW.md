# ReMind Repository & Git Discipline Specification

This document reflects the actual **ReMind** repository branch structure and commit history conventions.

---

## 1. REST API Versioning Policy

- **URL Prefix Scheme**: All public REST endpoints are explicitly scoped under `/api/v<N>/` (e.g., `/api/v1/words`, `/api/v1/words/due`, `/api/v1/srs/review`).
- **Telemetry & Monitoring**: The `/health` endpoint exposes `apiVersion: "v1"` in telemetry payloads.

---

## 2. Active Git Branch Strategy

Based on project repository branches (`git branch -a`):

| Branch Name                | Purpose                                                                                                                    |
|:---------------------------|:---------------------------------------------------------------------------------------------------------------------------|
| `master`                   | Main production/stable branch.                                                                                             |
| `feature/<feature-name>`   | New features, new architectural layers (like fullstack Ktor backend), or major capabilities isolated from the main branch. | 
---

## 3. Project Commit Guidelines

Commit messages in this repository use concise action-based descriptions:

- `added <feature/change>` (e.g., `added cancellation support`, `added Error Handling & Exception Management`)
- `updated <feature/component>` (e.g., `updated Logging & Monitoring`, `updated automatic device detection`)
- `fixed <issue/bug>` (e.g., `fixed repository fallbacks`, `fixed backend query`, `fixed network connectivity`)
- `implemented <class/feature>` (e.g., `implemented KtorVocabularyRepository...`, `created RemindApiClient...`)

---

## 4. Secret & Credential Protection

- **Ignored Files**: Service account keys (`*.json`, `serviceAccountKey*.json`, `service-account-key*.json`), `.env` files, and `.credentials/` directories are strictly excluded from Git tracking via `.gitignore`.
- **Pre-commit Check**: Verify `git status` before committing to ensure no credentials or local test files are staged.
