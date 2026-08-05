# OCR Expense Tracker

A Spring Boot application that uses Azure Document Intelligence and Azure OpenAI to automatically extract and structure receipt information from images.

## Features

- Upload receipt images through the new Receipt OCR Viewer web UI or the REST API
- Extract OCR data using Azure Document Intelligence
- View extracted fields in a card-based Thymeleaf experience with a results table and pretty-printed JSON output
- Parse and categorize receipt information using Azure OpenAI LLM
- Return structured JSON with merchant, date, amount, tax, category, and line items

## Prerequisites

- Java 25+
- Maven 3.8+
- Azure Document Intelligence resource
- Azure OpenAI resource with a deployed model

## Azure Setup

### 1. Azure Document Intelligence

1. Create a resource in Azure portal
2. Get your endpoint and API key
3. Note the endpoint format: `https://<region>.api.cognitive.microsoft.com/`

### 2. Azure OpenAI

1. Create an Azure OpenAI resource
2. Deploy a model (e.g., gpt-4, gpt-35-turbo)
3. Get your endpoint, API key, and deployment name

## Configuration

Set the following environment variables or update `src/main/resources/application.yml`:

```bash
export AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT=https://your-endpoint.cognitiveservices.azure.com/
export AZURE_DOCUMENT_INTELLIGENCE_KEY=your-api-key
export AZURE_OPENAI_ENDPOINT=https://your-endpoint.openai.azure.com/
export AZURE_OPENAI_KEY=your-api-key
export AZURE_OPENAI_DEPLOYMENT=your-deployment-name
export AZURE_OPENAI_API_VERSION=2024-02-15-preview
```

Or directly in `application.yml`:

```yaml
azure:
  document-intelligence:
    endpoint: https://your-endpoint.cognitiveservices.azure.com/
    api-key: your-api-key
  open-ai:
    endpoint: https://your-endpoint.openai.azure.com/
    api-key: your-api-key
    deployment-name: your-deployment-name
    api-version: 2024-02-15-preview
```

## Build and Run

### Build

```bash
mvn clean package
```

### Run

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

Open `http://localhost:8080/` to use the Receipt OCR Viewer. Select a receipt image, click "Analyze Receipt", and review the extracted fields and JSON output on the same page. The form submits to `/upload` and renders the results in a card-based UI.

## Web UI

- Browse the upload screen at `/`
- Submit a receipt image to `/upload`
- Review the uploaded filename, success/error messages, extracted field table, and pretty-printed JSON response

## API Endpoints

### Health Check

```bash
GET /api/receipts/health
```

### Upload Receipt

```bash
POST /api/receipts/upload
Content-Type: multipart/form-data

Parameter: file (image file - jpg, png, pdf)
```

**Response:**

```json
{
  "merchant": "Whole Foods Market",
  "date": "2026-01-15",
  "totalAmount": 42.50,
  "tax": 3.50,
  "category": "GROCERIES",
  "items": [
    {"name": "Organic Apples", "price": 5.99},
    {"name": "Almond Milk", "price": 4.99},
    {"name": "Greek Yogurt", "price": 6.99}
  ]
}
```

## Response Schema

The receipt extraction returns:

- **merchant** (string): Name of the store/restaurant
- **date** (YYYY-MM-DD): Transaction date
- **totalAmount** (number): Total amount paid
- **tax** (number): Tax amount
- **category** (enum): One of GROCERIES, ENTERTAINMENT, TRANSPORT, DINING, OTHER
- **items** (array): List of purchased items with name and price

## Testing with cURL

```bash
curl -X POST http://localhost:8080/api/receipts/upload \
  -F "file=@/path/to/receipt.jpg"
```

## Docker

### Build Docker Image

```bash
docker build -t ocr-expense-tracker:latest .
```

### Run in Docker

```bash
docker run -p 8080:8080 \
  -e AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT=https://your-endpoint.cognitiveservices.azure.com/ \
  -e AZURE_DOCUMENT_INTELLIGENCE_KEY=your-api-key \
  -e AZURE_OPENAI_ENDPOINT=https://your-endpoint.openai.azure.com/ \
  -e AZURE_OPENAI_KEY=your-api-key \
  -e AZURE_OPENAI_DEPLOYMENT=your-deployment-name \
  ocr-expense-tracker:latest
```

## Troubleshooting

### "Authentication failed" errors
- Verify Azure endpoints are correct (no trailing slashes)
- Check API keys are valid and not expired
- Ensure resources are in active state

### "Invalid model deployment name"
- Confirm deployment name matches your Azure OpenAI deployment

### "File too large" error
- Default max file size is 10MB, configured in application.yml

## License

MIT
