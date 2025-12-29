# Contributing to Audit Trail Universel

Merci de votre interet pour contribuer a Audit Trail !

## Table des Matieres

- [Code de Conduite](#code-de-conduite)
- [Comment Contribuer](#comment-contribuer)
- [Configuration de l'Environnement](#configuration-de-lenvironnement)
- [Standards de Code](#standards-de-code)
- [Process de Pull Request](#process-de-pull-request)
- [Reporting de Bugs](#reporting-de-bugs)
- [Demandes de Features](#demandes-de-features)

## Code de Conduite

Ce projet adhere au [Contributor Covenant](https://www.contributor-covenant.org/). En participant, vous vous engagez a respecter ce code.

## Comment Contribuer

### Types de contributions

- **Bug fixes** - Corrections de bugs
- **Features** - Nouvelles fonctionnalites
- **Documentation** - Ameliorations de la documentation
- **Tests** - Ajout de tests
- **Refactoring** - Amelioration du code existant

### Workflow

1. **Fork** le repository
2. **Clone** votre fork
3. **Creez une branche** depuis `main`
4. **Developpez** votre contribution
5. **Testez** vos changements
6. **Committez** avec un message conventionnel
7. **Push** votre branche
8. **Ouvrez une Pull Request**

## Configuration de l'Environnement

### Prerequis

- Java 21 (JDK)
- Maven 3.9+
- Docker & Docker Compose
- Node.js 20+ (pour SDK JS)
- Python 3.11+ (pour SDK Python)
- Go 1.21+ (pour SDK Go)

### Setup

```bash
# Clone
git clone https://github.com/YOUR_USERNAME/audit-trail-server.git
cd audit-trail-server

# Demarrer les dependances
docker-compose -f docker/docker-compose.yml up -d postgres elasticsearch redis

# Build
mvn clean install

# Tests
mvn test
```

### Commandes utiles

```bash
# Build sans tests
mvn clean package -DskipTests

# Tests unitaires
mvn test -Dtest="!*IntegrationTest"

# Tests integration
mvn test -Dtest="*IntegrationTest"

# Coverage report
mvn test jacoco:report

# Demarrer l'application
mvn spring-boot:run -pl audit-trail-app
```

## Standards de Code

### Java

- **Style** : Google Java Style Guide
- **Pas de commentaires** dans le code (code auto-documente)
- **Tests** : JUnit 5 + Mockito + AssertJ
- **Coverage** : Minimum 80%

### Structure des tests

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("MyService Tests")
class MyServiceTest {

    @Mock
    private MyRepository repository;

    @InjectMocks
    private MyService service;

    @Nested
    @DisplayName("methodName() Tests")
    class MethodNameTests {

        @Test
        @DisplayName("Should do something when condition")
        void shouldDoSomethingWhenCondition() {
            // given
            // when
            // then
        }
    }
}
```

### Commits

Suivez [Conventional Commits](https://www.conventionalcommits.org/) :

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types** :
- `feat` : Nouvelle fonctionnalite
- `fix` : Correction de bug
- `docs` : Documentation
- `test` : Tests
- `refactor` : Refactoring
- `chore` : Maintenance
- `perf` : Performance

**Exemples** :

```
feat(search): add full-text search support
fix(storage): handle null previousHash for first event
docs(readme): update quick start section
test(admin): add API key rotation tests
refactor(core): extract hash calculation to utility class
```

### Branches

- `main` - Production
- `develop` - Developpement
- `feature/*` - Nouvelles fonctionnalites
- `fix/*` - Corrections de bugs
- `docs/*` - Documentation
- `refactor/*` - Refactoring

## Process de Pull Request

### Avant de soumettre

1. **Assurez-vous que les tests passent** : `mvn test`
2. **Verifiez le coverage** : `mvn test jacoco:report`
3. **Mettez a jour la documentation** si necessaire
4. **Rebasez sur main** si necessaire

### Description de la PR

Utilisez le template suivant :

```markdown
## Description
[Description claire des changements]

## Type de changement
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Checklist
- [ ] Tests ajoutes/mis a jour
- [ ] Documentation mise a jour
- [ ] Pas de code commente
- [ ] Commit messages conventionnels
- [ ] Branch a jour avec `main`

## Tests effectues
[Description des tests]

## Screenshots (si applicable)
[Screenshots]
```

### Review process

1. Au moins 1 review approuvee requise
2. Tous les checks CI doivent passer
3. Pas de conflits avec `main`
4. Le mainteneur mergera via "Squash and merge"

## Reporting de Bugs

Utilisez le template d'issue "Bug Report" avec :

- **Description** claire du bug
- **Steps to reproduce**
- **Expected behavior**
- **Actual behavior**
- **Environment** (OS, Java version, etc.)
- **Logs** si disponibles

### Template

```markdown
## Description
[Description claire du bug]

## Steps to Reproduce
1. Step 1
2. Step 2
3. ...

## Expected Behavior
[Ce qui devrait se passer]

## Actual Behavior
[Ce qui se passe]

## Environment
- OS: [e.g., macOS 14.0]
- Java: [e.g., 21.0.1]
- Version: [e.g., 1.0.0]

## Logs
```
[Logs ici]
```
```

## Demandes de Features

Utilisez le template d'issue "Feature Request" avec :

- **Description** de la feature
- **Motivation** / cas d'usage
- **Proposed solution** (optionnel)
- **Alternatives considered** (optionnel)

### Template

```markdown
## Description
[Description de la feature]

## Motivation
[Pourquoi cette feature est necessaire]

## Proposed Solution
[Solution proposee - optionnel]

## Alternatives Considered
[Alternatives envisagees - optionnel]

## Additional Context
[Contexte additionnel]
```

---

## Questions ?

- [Discussions GitHub](https://github.com/Mohmk10/audit-trail-server/discussions)
- Issues pour bugs/features
- contact@mohmk10.com

Merci de contribuer !
