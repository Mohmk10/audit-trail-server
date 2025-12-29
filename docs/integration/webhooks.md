# Webhooks Integration

Guide d'integration des webhooks pour recevoir des evenements en temps reel.

## Vue d'ensemble

Les webhooks permettent a votre application de recevoir des notifications lorsque des evenements se produisent dans Audit Trail.

```
Audit Trail              Votre Application
    │                           │
    │  Event occurs             │
    ├──────────────────────────►│
    │  POST /your-webhook       │
    │                           │
    │◄──────────────────────────┤
    │  200 OK                   │
    │                           │
```

## Configuration

### Creer un webhook

```bash
curl -X POST https://api.audit-trail.io/api/v1/webhooks \
  -H "X-API-Key: your-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-001",
    "name": "My Webhook",
    "url": "https://myapp.com/webhooks/audit-trail",
    "secret": "whsec_abc123def456",
    "events": ["event.created", "alert.triggered"],
    "enabled": true
  }'
```

### Types d'evenements

| Evenement | Description |
|-----------|-------------|
| `event.created` | Nouvel evenement d'audit cree |
| `event.batch.created` | Batch d'evenements cree |
| `alert.triggered` | Alerte declenchee par une regle |
| `alert.resolved` | Alerte resolue |
| `report.completed` | Rapport genere |
| `report.failed` | Echec de generation de rapport |

## Payload

### Structure

```json
{
  "id": "whd_abc123",
  "type": "event.created",
  "timestamp": "2025-01-15T10:30:00Z",
  "tenantId": "tenant-001",
  "data": {
    "event": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "timestamp": "2025-01-15T10:30:00Z",
      "actor": {
        "id": "user-123",
        "type": "USER",
        "name": "John Doe"
      },
      "action": {
        "type": "CREATE",
        "description": "Created document"
      },
      "resource": {
        "id": "doc-456",
        "type": "DOCUMENT",
        "name": "Q4 Report"
      }
    }
  }
}
```

### Exemples par type

#### event.created

```json
{
  "id": "whd_abc123",
  "type": "event.created",
  "timestamp": "2025-01-15T10:30:00Z",
  "tenantId": "tenant-001",
  "data": {
    "event": { ... }
  }
}
```

#### alert.triggered

```json
{
  "id": "whd_def456",
  "type": "alert.triggered",
  "timestamp": "2025-01-15T10:30:00Z",
  "tenantId": "tenant-001",
  "data": {
    "alert": {
      "id": "550e8400-...",
      "ruleName": "Failed Login Detection",
      "severity": "HIGH",
      "title": "Multiple failed logins detected",
      "description": "5 failed login attempts from user-123"
    },
    "triggeringEvents": [
      { "id": "event-1", ... },
      { "id": "event-2", ... }
    ]
  }
}
```

## Verification de signature

Chaque requete webhook inclut une signature pour verifier l'authenticite.

### Headers

```
X-Audit-Trail-Signature: sha256=abc123...
X-Audit-Trail-Timestamp: 1705312200
X-Audit-Trail-Delivery-Id: whd_abc123
```

### Verification (Node.js)

```javascript
const crypto = require('crypto');

function verifyWebhookSignature(payload, signature, timestamp, secret) {
  const signedPayload = `${timestamp}.${JSON.stringify(payload)}`;
  const expectedSignature = crypto
    .createHmac('sha256', secret)
    .update(signedPayload)
    .digest('hex');

  const expected = `sha256=${expectedSignature}`;

  // Utiliser timingSafeEqual pour eviter timing attacks
  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(expected)
  );
}

// Utilisation dans Express
app.post('/webhooks/audit-trail', (req, res) => {
  const signature = req.headers['x-audit-trail-signature'];
  const timestamp = req.headers['x-audit-trail-timestamp'];

  if (!verifyWebhookSignature(req.body, signature, timestamp, WEBHOOK_SECRET)) {
    return res.status(401).send('Invalid signature');
  }

  // Traiter le webhook
  console.log('Webhook verified:', req.body.type);
  res.status(200).send('OK');
});
```

### Verification (Python)

```python
import hmac
import hashlib

def verify_webhook_signature(payload: str, signature: str, timestamp: str, secret: str) -> bool:
    signed_payload = f"{timestamp}.{payload}"
    expected = hmac.new(
        secret.encode(),
        signed_payload.encode(),
        hashlib.sha256
    ).hexdigest()

    expected_signature = f"sha256={expected}"
    return hmac.compare_digest(signature, expected_signature)

# Utilisation avec Flask
@app.route('/webhooks/audit-trail', methods=['POST'])
def handle_webhook():
    signature = request.headers.get('X-Audit-Trail-Signature')
    timestamp = request.headers.get('X-Audit-Trail-Timestamp')

    if not verify_webhook_signature(
        request.data.decode(),
        signature,
        timestamp,
        WEBHOOK_SECRET
    ):
        return 'Invalid signature', 401

    payload = request.json
    print(f"Webhook verified: {payload['type']}")
    return 'OK', 200
```

### Verification (Java)

```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.HexFormat;

public class WebhookVerifier {

    public boolean verifySignature(String payload, String signature,
                                   String timestamp, String secret) {
        try {
            String signedPayload = timestamp + "." + payload;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(), "HmacSHA256");
            mac.init(keySpec);

            byte[] hash = mac.doFinal(signedPayload.getBytes());
            String expected = "sha256=" + HexFormat.of().formatHex(hash);

            return MessageDigest.isEqual(
                signature.getBytes(),
                expected.getBytes()
            );
        } catch (Exception e) {
            return false;
        }
    }
}
```

## Retry Policy

Si votre endpoint ne repond pas avec un code 2xx, Audit Trail reessaiera :

| Tentative | Delai |
|-----------|-------|
| 1 | Immediate |
| 2 | 1 minute |
| 3 | 5 minutes |
| 4 | 30 minutes |
| 5 | 2 heures |

Apres 5 echecs, le webhook est desactive automatiquement.

## Best Practices

### 1. Repondre rapidement

```javascript
app.post('/webhooks/audit-trail', async (req, res) => {
  // Repondre immediatement
  res.status(200).send('OK');

  // Traiter de maniere asynchrone
  processWebhook(req.body).catch(err => {
    console.error('Webhook processing failed:', err);
  });
});
```

### 2. Idempotence

Utilisez le `X-Audit-Trail-Delivery-Id` pour eviter les doublons :

```javascript
const processedDeliveries = new Set();

app.post('/webhooks/audit-trail', (req, res) => {
  const deliveryId = req.headers['x-audit-trail-delivery-id'];

  if (processedDeliveries.has(deliveryId)) {
    return res.status(200).send('Already processed');
  }

  processedDeliveries.add(deliveryId);
  // Traiter...
  res.status(200).send('OK');
});
```

### 3. Validation du timestamp

Rejeter les webhooks trop anciens :

```javascript
const MAX_AGE = 5 * 60 * 1000; // 5 minutes

app.post('/webhooks/audit-trail', (req, res) => {
  const timestamp = parseInt(req.headers['x-audit-trail-timestamp']) * 1000;
  const now = Date.now();

  if (now - timestamp > MAX_AGE) {
    return res.status(400).send('Webhook too old');
  }

  // Traiter...
});
```

### 4. File d'attente

Pour les volumes importants, utilisez une file d'attente :

```javascript
const Queue = require('bull');
const webhookQueue = new Queue('webhooks');

app.post('/webhooks/audit-trail', (req, res) => {
  // Ajouter a la file
  webhookQueue.add(req.body);
  res.status(200).send('OK');
});

webhookQueue.process(async (job) => {
  const payload = job.data;
  await processWebhook(payload);
});
```

## Testing

### Endpoint de test

```bash
# Generer un webhook de test
curl -X POST https://api.audit-trail.io/api/v1/webhooks/{id}/test \
  -H "X-API-Key: your-api-key"
```

### Outil local

Utilisez un tunnel pour tester en local :

```bash
# Avec ngrok
ngrok http 3000

# Ou avec localtunnel
npx localtunnel --port 3000
```

## Monitoring

### Verifier l'etat des livraisons

```bash
curl https://api.audit-trail.io/api/v1/webhooks/{id}/deliveries \
  -H "X-API-Key: your-api-key"
```

### Reponse

```json
{
  "deliveries": [
    {
      "id": "whd_abc123",
      "timestamp": "2025-01-15T10:30:00Z",
      "status": 200,
      "duration": 145,
      "success": true
    },
    {
      "id": "whd_def456",
      "timestamp": "2025-01-15T10:25:00Z",
      "status": 500,
      "duration": 5000,
      "success": false,
      "retryAt": "2025-01-15T10:30:00Z"
    }
  ]
}
```
