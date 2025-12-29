# 🚀 Guide de Release v1.0.0

Ce guide explique comment configurer tous les secrets et publier la première version d'Audit Trail.

## Table des Matières

1. [Prérequis](#1-prérequis)
2. [Docker Hub](#2-docker-hub)
3. [Maven Central (Sonatype)](#3-maven-central-sonatype)
4. [GPG Key](#4-gpg-key)
5. [npm](#5-npm)
6. [PyPI](#6-pypi)
7. [Configurer les Secrets GitHub](#7-configurer-les-secrets-github)
8. [Premier Release](#8-premier-release)
9. [Vérification](#9-vérification)

---

## 1. Prérequis

- Compte [GitHub](https://github.com)
- Compte [Docker Hub](https://hub.docker.com)
- Compte [Sonatype OSSRH](https://central.sonatype.org) (Maven Central)
- Compte [npm](https://www.npmjs.com)
- Compte [PyPI](https://pypi.org)
- GPG installé sur ta machine

---

## 2. Docker Hub

### 2.1 Créer un compte

1. Va sur [https://hub.docker.com](https://hub.docker.com)
2. Clique sur **Sign Up**
3. Crée ton compte (username: `mohmk10` par exemple)

### 2.2 Créer un Access Token

1. Connecte-toi à Docker Hub
2. Clique sur ton avatar en haut à droite → **Account Settings**
3. Dans le menu gauche, clique sur **Security**
4. Clique sur **New Access Token**
5. Configure :
   - **Description** : `GitHub Actions - Audit Trail`
   - **Access permissions** : `Read, Write, Delete`
6. Clique sur **Generate**
7. **COPIE LE TOKEN** (il ne sera plus affiché)

### Secrets à noter :

| Secret | Valeur |
|--------|--------|
| `DOCKERHUB_USERNAME` | Ton username Docker Hub (ex: `mohmk10`) |
| `DOCKERHUB_TOKEN` | Le token que tu viens de générer |

---

## 3. Maven Central (Sonatype)

### 3.1 Créer un compte Sonatype

1. Va sur [https://central.sonatype.org](https://central.sonatype.org)
2. Clique sur **Sign In** puis **Sign Up**
3. Crée ton compte avec ton email

### 3.2 Créer un namespace (Group ID)

Pour publier sur Maven Central, tu dois "prouver" que tu possèdes le domaine/namespace.

**Option A : Utiliser GitHub (recommandé)**

1. Connecte-toi au [Sonatype Portal](https://central.sonatype.org)
2. Va dans **Namespaces**
3. Clique sur **Add Namespace**
4. Choisis **GitHub** comme méthode de vérification
5. Entre : `io.github.mohmk10`
6. Suis les instructions pour créer un repo temporaire de vérification

**Option B : Utiliser un domaine personnel**

Si tu as un domaine (ex: mohmk10.io), tu peux l'utiliser comme Group ID.

### 3.3 Générer un User Token

1. Connecte-toi au [Sonatype Portal](https://central.sonatype.org)
2. Va dans **Account** → **Generate User Token**
3. Copie le **Username** et **Password** générés

### Secrets à noter :

| Secret | Valeur |
|--------|--------|
| `OSSRH_USERNAME` | Le username du User Token (pas ton email !) |
| `OSSRH_TOKEN` | Le password du User Token |

---

## 4. GPG Key

Maven Central exige que les JARs soient signés avec GPG.

### 4.1 Installer GPG

**macOS :**
```bash
brew install gnupg
```

**Ubuntu/Debian :**
```bash
sudo apt-get install gnupg
```

**Windows :**
Télécharge [Gpg4win](https://www.gpg4win.org/)

### 4.2 Générer une clé GPG

```bash
gpg --full-generate-key
```

Choisis :
1. **(1) RSA and RSA**
2. **4096** bits
3. **0** (la clé n'expire pas)
4. **Ton nom** : Mohamed
5. **Ton email** : ton-email@example.com
6. **Passphrase** : Choisis un mot de passe fort (note-le !)

### 4.3 Lister les clés

```bash
gpg --list-secret-keys --keyid-format LONG
```

Output exemple :
```
sec   rsa4096/ABC123DEF456789 2025-01-15 [SC]
      1234567890ABCDEF1234567890ABCDEF12345678
uid                 [ultimate] Mohamed <ton-email@example.com>
ssb   rsa4096/DEF789ABC123456 2025-01-15 [E]
```

Note le **Key ID** : `ABC123DEF456789` (après `rsa4096/`)

### 4.4 Publier la clé sur un serveur de clés

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys ABC123DEF456789
gpg --keyserver keys.openpgp.org --send-keys ABC123DEF456789
```

### 4.5 Exporter la clé privée

```bash
gpg --armor --export-secret-keys ABC123DEF456789 > private-key.asc
```

Affiche le contenu :
```bash
cat private-key.asc
```

Le contenu ressemble à :
```
-----BEGIN PGP PRIVATE KEY BLOCK-----

lQdGBGX...
...
-----END PGP PRIVATE KEY BLOCK-----
```

### Secrets à noter :

| Secret | Valeur |
|--------|--------|
| `GPG_PRIVATE_KEY` | Tout le contenu de `private-key.asc` (avec BEGIN/END) |
| `GPG_PASSPHRASE` | Le mot de passe que tu as choisi |

### 4.6 Nettoyer

```bash
rm private-key.asc  # Supprime le fichier local après avoir copié le contenu
```

---

## 5. npm

### 5.1 Créer un compte

1. Va sur [https://www.npmjs.com](https://www.npmjs.com)
2. Clique sur **Sign Up**
3. Crée ton compte

### 5.2 Créer un Access Token

1. Connecte-toi à npm
2. Clique sur ton avatar → **Access Tokens**
3. Clique sur **Generate New Token** → **Classic Token**
4. Choisis **Automation** (pour CI/CD)
5. **COPIE LE TOKEN**

### 5.3 (Optionnel) Activer 2FA pour la publication

Si tu as activé 2FA sur npm :
1. Va dans **Account Settings**
2. Dans **Two-Factor Authentication**, choisis **Authorization only**
   (pas "Authorization and Publishing" sinon le CI ne pourra pas publier)

### Secret à noter :

| Secret | Valeur |
|--------|--------|
| `NPM_TOKEN` | Le token que tu viens de générer |

---

## 6. PyPI

### 6.1 Créer un compte

1. Va sur [https://pypi.org](https://pypi.org)
2. Clique sur **Register**
3. Crée ton compte
4. **Vérifie ton email**

### 6.2 Activer 2FA (recommandé)

1. Va dans **Account Settings**
2. Active **Two-Factor Authentication**
3. Utilise une app comme Google Authenticator

### 6.3 Créer un API Token

1. Va dans **Account Settings**
2. Scroll jusqu'à **API tokens**
3. Clique sur **Add API token**
4. Configure :
   - **Token name** : `GitHub Actions - Audit Trail`
   - **Scope** : `Entire account` (pour la première fois)
5. Clique sur **Add token**
6. **COPIE LE TOKEN** (commence par `pypi-`)

### Secret à noter :

| Secret | Valeur |
|--------|--------|
| `PYPI_TOKEN` | Le token (commence par `pypi-...`) |

---

## 7. Configurer les Secrets GitHub

### 7.1 Accéder aux secrets

1. Va sur ton repo GitHub : `https://github.com/devmohmk/audit-trail-server`
2. Clique sur **Settings** (onglet)
3. Dans le menu gauche : **Secrets and variables** → **Actions**
4. Clique sur **New repository secret**

### 7.2 Ajouter chaque secret

Ajoute ces 8 secrets un par un :

| Name | Value |
|------|-------|
| `DOCKERHUB_USERNAME` | `mohmk10` (ton username) |
| `DOCKERHUB_TOKEN` | `dckr_pat_xxxxx...` |
| `OSSRH_USERNAME` | Le username du User Token Sonatype |
| `OSSRH_TOKEN` | Le password du User Token Sonatype |
| `GPG_PRIVATE_KEY` | Le contenu complet de `private-key.asc` |
| `GPG_PASSPHRASE` | Ton mot de passe GPG |
| `NPM_TOKEN` | `npm_xxxxx...` |
| `PYPI_TOKEN` | `pypi-xxxxx...` |

### 7.3 Vérifier

Tu devrais voir 8 secrets dans la liste :
```
DOCKERHUB_USERNAME      Updated just now
DOCKERHUB_TOKEN         Updated just now
OSSRH_USERNAME          Updated just now
OSSRH_TOKEN             Updated just now
GPG_PRIVATE_KEY         Updated just now
GPG_PASSPHRASE          Updated just now
NPM_TOKEN               Updated just now
PYPI_TOKEN              Updated just now
```

---

## 8. Premier Release

### 8.1 Vérifier que le CI passe

Avant de faire un release, assure-toi que tous les tests passent :

```bash
git push origin main
```

Va sur GitHub → Actions et vérifie que le workflow CI est vert ✅

### 8.2 Mettre à jour les versions

Avant le tag, mets à jour les versions dans les fichiers :

**pom.xml** (racine et modules) :
```xml
<version>1.0.0</version>
```

**audit-trail-sdk-js/package.json** :
```json
"version": "1.0.0"
```

**audit-trail-sdk-python/pyproject.toml** :
```toml
version = "1.0.0"
```

**audit-trail-sdk-go/go.mod** : (pas de version, utilise les tags)

### 8.3 Commit les changements de version

```bash
git add .
git commit -m "chore: bump version to 1.0.0"
git push origin main
```

### 8.4 Créer le tag

```bash
# Tag principal pour le serveur Docker
git tag -a v1.0.0 -m "Release v1.0.0 - Initial release

Features:
- Immutable event storage with SHA-256 hash chain
- ECDSA cryptographic signatures
- Elasticsearch-powered search
- Real-time detection and alerting
- PDF/CSV/Excel/JSON reporting
- Multi-tenant with RBAC
- Webhooks, Kafka, SIEM integration
- SDKs for Java, JavaScript, Python, Go"

# Push le tag
git push origin v1.0.0
```

### 8.5 Tags pour les SDKs (optionnel - séparés)

Si tu veux versionner les SDKs séparément :

```bash
# SDK Java
git tag -a sdk-java-v1.0.0 -m "SDK Java v1.0.0"
git push origin sdk-java-v1.0.0

# SDK JavaScript
git tag -a sdk-js-v1.0.0 -m "SDK JavaScript v1.0.0"
git push origin sdk-js-v1.0.0

# SDK Python
git tag -a sdk-python-v1.0.0 -m "SDK Python v1.0.0"
git push origin sdk-python-v1.0.0

# SDK Go
git tag -a sdk-go-v1.0.0 -m "SDK Go v1.0.0"
git push origin sdk-go-v1.0.0
```

### 8.6 Vérifier les workflows

Après avoir pushé les tags, va sur GitHub → Actions :

- ✅ `release-server` devrait se lancer → Docker Hub
- ✅ `release-sdk-java` → Maven Central
- ✅ `release-sdk-js` → npm
- ✅ `release-sdk-python` → PyPI
- ✅ `release-sdk-go` → Tag Go module

---

## 9. Vérification

### 9.1 Docker Hub

```bash
docker pull devmohmk/audit-trail-server:1.0.0
docker pull devmohmk/audit-trail-server:latest
```

Ou vérifie sur : https://hub.docker.com/r/devmohmk/audit-trail-server

### 9.2 Maven Central

Après quelques heures (la synchronisation peut prendre du temps) :

```bash
# Dans un projet Java
mvn dependency:get -Dartifact=io.github.mohmk10:audit-trail-sdk:1.0.0
```

Ou cherche sur : https://search.maven.org/artifact/io.github.mohmk10/audit-trail-sdk

### 9.3 npm

```bash
npm view @mohmk10/audit-trail-sdk
npm install @mohmk10/audit-trail-sdk@1.0.0
```

Ou vérifie sur : https://www.npmjs.com/package/@mohmk10/audit-trail-sdk

### 9.4 PyPI

```bash
pip install audit-trail-sdk==1.0.0
```

Ou vérifie sur : https://pypi.org/project/audit-trail-sdk/

### 9.5 Go

```bash
go get github.com/devmohmk/audit-trail-server/audit-trail-sdk-go@v1.0.0
```

---

## Troubleshooting

### Docker Hub : "unauthorized"

- Vérifie que `DOCKERHUB_USERNAME` et `DOCKERHUB_TOKEN` sont corrects
- Le token doit avoir les permissions Read/Write

### Maven Central : "401 Unauthorized"

- Le User Token expire après un certain temps, régénère-le si besoin
- Vérifie que ton namespace est bien vérifié

### Maven Central : "GPG signature failed"

- Vérifie que la clé GPG est bien publiée sur les serveurs de clés
- Attends quelques minutes après la publication de la clé

### npm : "403 Forbidden"

- Si 2FA est activé avec "Authorization and Publishing", change en "Authorization only"
- Vérifie que le token n'a pas expiré

### PyPI : "403 Forbidden"

- Vérifie que ton email est vérifié
- Le token doit commencer par `pypi-`

---

## Checklist Finale

- [ ] Compte Docker Hub créé
- [ ] Token Docker Hub généré
- [ ] Compte Sonatype créé
- [ ] Namespace `io.github.mohmk10` vérifié
- [ ] User Token Sonatype généré
- [ ] Clé GPG générée et publiée
- [ ] Compte npm créé
- [ ] Token npm généré
- [ ] Compte PyPI créé et email vérifié
- [ ] Token PyPI généré
- [ ] 8 secrets configurés dans GitHub
- [ ] CI passe (vert)
- [ ] Versions mises à jour (1.0.0)
- [ ] Tag `v1.0.0` créé et pushé
- [ ] Workflows de release terminés
- [ ] Vérification sur chaque plateforme

---

## Commandes Récapitulatives

```bash
# Vérifier les secrets localement (ne jamais commiter !)
echo "DOCKERHUB_USERNAME=mohmk10"
echo "DOCKERHUB_TOKEN=dckr_pat_xxx"
# etc.

# Générer clé GPG
gpg --full-generate-key
gpg --list-secret-keys --keyid-format LONG
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
gpg --armor --export-secret-keys KEY_ID > private-key.asc

# Release
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# Vérifier les publications
docker pull devmohmk/audit-trail-server:1.0.0
npm view @mohmk10/audit-trail-sdk
pip install audit-trail-sdk==1.0.0
```

---

🎉 **Félicitations ! Tu as publié Audit Trail v1.0.0 !**
