"""
fix_tech_family.py — Заполняет поле tech_family в skill_canonical
по правилам (без LLM), на основе точного совпадения имён.

Использование:
    python fix_tech_family.py              # preview: показать что изменится
    python fix_tech_family.py --save       # применить изменения в БД
    python fix_tech_family.py --family Python   # только одно семейство
    python fix_tech_family.py --domain BACKEND  # ограничить доменом
    python fix_tech_family.py --show-unknown    # навыки без tech_family
"""

import argparse
import os
import sys

from dotenv import load_dotenv

load_dotenv()

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

DB_URL = os.getenv("DB_URL", "")

# ─── Правила: tech_family → список точных имён (case-insensitive) ─────────────
#
# Структура:
#   "Family Name": {
#       "exact": [...],       # точное совпадение (ILIKE без %)
#       "starts": [...],      # name ILIKE 'prefix%'
#       "contains": [...],    # name ILIKE '%substring%'  (осторожно: шире)
#   }

RULES: dict[str, dict] = {

    # ══════════════════════════════════════════════════════════════
    # BACKEND: языки и фреймворки
    # ══════════════════════════════════════════════════════════════

    "Python": {
        "exact": [
            "FastAPI", "Django", "Flask", "Tornado", "Starlette", "Sanic",
            "Bottle", "Falcon", "Pyramid", "CherryPy", "Litestar", "BlackSheep",
            "Quart", "aiohttp",
            "Django REST Framework", "Django Rest Framework", "DRF",
            "django-rest-framework",
            "SQLAlchemy", "Alembic", "Django ORM", "SQLModel", "Peewee",
            "Tortoise ORM", "Pony ORM", "Beanie",
            "asyncio", "Celery", "Kombu", "aioredis", "aiofiles",
            "Dramatiq", "Huey", "RQ", "Arq", "FastStream", "Propan",
            "requests", "httpx", "urllib3",
            "Pydantic", "Pydantic v2", "Marshmallow", "attrs", "dataclasses-json",
            "uvicorn", "gunicorn", "Hypercorn", "Daphne", "Waitress", "uWSGI",
            "python-jose", "PyJWT", "passlib", "authlib",
            "MongoEngine", "Motor", "PyMongo",
            "kafka-python", "Pika", "aio-pika",
            "Pillow", "python-multipart", "python-dotenv",
            "websockets", "channels", "Django Channels",
            "Strawberry", "Graphene",
        ],
        "starts": [
            "Pydantic", "Django ", "FastAPI ", "Flask-", "SQLAlchemy",
            "python-", "aiohttp", "aioredis", "aiofiles",
        ],
        "contains": [],
    },

    ".NET": {
        "exact": [
            ".NET", ".NET Core", ".NET Framework", "ASP.NET", "ASP.NET Core",
            "ASP.NET MVC", "ASP.NET Web API", "Blazor", "MAUI", ".NET MAUI",
            "Entity Framework", "Entity Framework Core", "EF Core",
            "SignalR", "WPF", "WinForms", "Windows Forms",
            "NHibernate", "Dapper", "AutoMapper",
            "MediatR", "FluentValidation", "Polly",
            "Serilog", "NLog", "Log4Net",
            "xUnit", "NUnit", "MSTest", "Moq",
            "Swagger", "Swashbuckle", "NSwag",
            "Hangfire", "Quartz.NET",
            "IdentityServer", "Duende IdentityServer",
            "Carter", "Minimal API",
        ],
        "starts": [".NET", "ASP.NET", "Entity Framework"],
        "contains": [],
    },

    "Java": {
        "exact": [
            "Spring", "Spring Boot", "Spring MVC", "Spring Data", "Spring Security",
            "Spring Cloud", "Spring Batch", "Spring Integration",
            "Hibernate", "Hibernate Validator", "JPA", "JPQL", "JDBC", "Spring Data JPA",
            "MyBatis", "iBatis",
            "Maven", "Gradle",
            "JUnit", "Mockito", "TestContainers",
            "Lombok", "MapStruct",
            "Jackson", "Gson",
            "Quarkus", "Micronaut", "Vert.x", "Jakarta EE",
            "Kafka Streams", "Apache Camel",
            "Liquibase", "Flyway",
        ],
        "starts": ["Spring ", "Spring."],
        "contains": [],
    },

    "Go": {
        "exact": [
            "Go", "Golang", "Gin", "Echo", "Fiber", "Chi", "Gorilla Mux",
            "GORM", "sqlx", "pgx",
            "Go Kit", "go-kit",
            "Testify", "GoMock",
            "Goroutines", "gRPC-Go",
            "Cobra", "Viper",
        ],
        "starts": [],
        "contains": [],
    },

    "Node.js": {
        "exact": [
            "Node.js", "NodeJS", "Node JS",
            "Express", "Express.js", "NestJS", "Nest.js", "Fastify",
            "Koa", "Hapi", "Restify", "Sails.js",
            "Sequelize", "TypeORM", "Prisma", "Mongoose",
            "Knex", "Knex.js", "knex",
            "Socket.io", "socket.io",
            "PM2", "Nodemon",
            "Bull", "BullMQ", "Agenda",
            "Passport.js", "jsonwebtoken",
        ],
        "starts": ["Express", "NestJS", "Node"],
        "contains": [],
    },

    "PHP": {
        "exact": [
            "PHP", "Laravel", "Symfony", "Yii", "Yii2", "CodeIgniter",
            "Zend", "CakePHP", "Slim",
            "Doctrine", "Eloquent",
            "Composer", "PHPUnit",
            "WordPress", "Bitrix", "1С-Битрикс",
        ],
        "starts": ["PHP", "Laravel", "Symfony", "Yii"],
        "contains": [],
    },

    "Rust": {
        "exact": [
            "Rust", "Actix", "Actix-web", "Axum", "Rocket", "Warp",
            "Tokio", "async-std",
            "Diesel", "SeaORM", "SQLx",
            "Serde", "Cargo",
        ],
        "starts": [],
        "contains": [],
    },

    "Kotlin": {
        "exact": [
            "Kotlin", "Ktor", "Exposed",
            "Kotlin Coroutines", "kotlinx.coroutines",
            "Spring Boot (Kotlin)",
        ],
        "starts": ["Kotlin"],
        "contains": [],
    },

    "Ruby": {
        "exact": [
            "Ruby", "Ruby on Rails", "Rails", "Sinatra", "Hanami",
            "ActiveRecord", "RSpec", "Capybara", "Sidekiq", "Bundler",
        ],
        "starts": [],
        "contains": [],
    },

    "Scala": {
        "exact": [
            "Scala", "Akka", "Akka HTTP", "Akka Streams", "Play Framework",
            "Cats", "ZIO", "Shapeless", "Slick", "sbt",
            "Cats Effect", "fs2", "Tapir", "http4s",
        ],
        "starts": ["Akka", "Scala"],
        "contains": [],
    },

    "gRPC/Протоколы": {
        "exact": [
            "gRPC", "Protobuf", "Protocol Buffers", "Apache Thrift", "Thrift",
            "GraphQL", "REST", "RESTful", "REST API", "WebSocket", "WebSockets",
            "SOAP", "XML-RPC", "JSON-RPC", "OpenAPI", "Swagger",
            "AsyncAPI", "API Gateway", "API Design", "API-first",
            "API First Approach", "Contract-First", "API Contracts",
            "API Versioning", "APIVersioning", "API Версионирование",
        ],
        "starts": ["gRPC", "REST", "GraphQL", "OpenAPI"],
        "contains": [],
    },

    # ══════════════════════════════════════════════════════════════
    # DEVOPS: инфраструктура и автоматизация
    # ══════════════════════════════════════════════════════════════

    "CI/CD": {
        "exact": [
            "Jenkins", "GitLab CI", "GitLab CI/CD", "GitHub Actions",
            "CircleCI", "Travis CI", "TeamCity", "Bamboo", "Buildkite",
            "Drone CI", "Concourse", "Argo CD", "ArgoCD", "Argo Workflows",
            "Argo Workflow", "Flux", "FluxCD",
            "Tekton", "Spinnaker", "Harness", "Azure DevOps", "Azure Pipelines",
            "Bitbucket Pipelines", "Gitlab Runner", "GitLab Runner",
            "Jenkins X", "Werf", "Woodpecker CI",
            "GitOps", "Continuous Integration", "Continuous Deployment",
            "Continuous Delivery", "Pipeline as Code",
            "ArcadiaCI", "Atlantis",
        ],
        "starts": ["ArgoCD", "Argo CD", "GitLab CI", "GitHub Actions"],
        "contains": ["CI/CD", "CI CD"],
    },

    "Контейнеры": {
        "exact": [
            "Docker", "Docker Compose", "Docker Swarm", "Kubernetes", "K8s",
            "Helm", "Helm Chart", "Helm Charts", "Kustomize",
            "Podman", "Containerd", "CRI-O", "runc",
            "Skaffold", "Tilt", "Draft",
            "OpenShift", "Rancher", "k3s", "minikube", "kind",
            "EKS", "GKE", "AKS", "OKE",
            "Service Mesh", "Istio", "Linkerd", "Consul Connect",
            "Envoy", "Envoy Proxy",
            "Operator SDK", "Kubernetes Operators",
        ],
        "starts": ["Docker", "Kubernetes", "Helm", "k8s", "K8s"],
        "contains": [],
    },

    "Config Management": {
        "exact": [
            "Ansible", "Puppet", "Chef", "SaltStack", "Salt",
            "Terraform", "OpenTofu", "Pulumi", "Crossplane",
            "Bicep", "CloudFormation", "AWS CloudFormation",
            "CDK", "AWS CDK", "Terraform Cloud", "Terragrunt",
            "Packer", "Vagrant",
            "Infrastructure as Code", "IaC",
        ],
        "starts": ["Terraform", "Ansible", "Pulumi"],
        "contains": ["Infrastructure as Code"],
    },

    "Мониторинг": {
        "exact": [
            "Prometheus", "Grafana", "Alertmanager", "Alert Manager",
            "Grafana Loki", "Loki", "Tempo", "Thanos", "VictoriaMetrics",
            "Zabbix", "Nagios", "Icinga", "Checkmk",
            "DataDog", "Datadog", "New Relic", "Dynatrace",
            "AppDynamics", "APM",
            "Elastic APM", "Jaeger", "Zipkin",
            "OpenTelemetry", "OpenTracing", "OpenCensus",
            "ELK Stack", "EFK Stack",
            "Graylog", "Splunk", "Fluentd", "Fluent Bit", "Logstash",
            "async-profiler",
        ],
        "starts": ["Grafana", "Prometheus", "OpenTelemetry"],
        "contains": [],
    },

    "Build Systems": {
        "exact": [
            "Make", "CMake", "Makefile",
            "Bazel", "Buck", "Pants",
            "Ninja", "Meson", "SCons",
            "MSBuild",
            "Artifactory", "Nexus", "JFrog",
            "Binary Release Management",
        ],
        "starts": ["Bazel", "CMake"],
        "contains": [],
    },

    # ══════════════════════════════════════════════════════════════
    # DATABASE
    # ══════════════════════════════════════════════════════════════

    "Реляционные": {
        "domain_restrict": "DATABASE",
        "exact": [
            "PostgreSQL", "MySQL", "MariaDB", "SQLite", "Oracle", "Oracle DB",
            "MS SQL", "MS SQL Server", "SQL Server", "MSSQL",
            "IBM DB2", "Sybase", "Teradata", "Firebird", "InterBase",
            "SQL", "PL/SQL", "T-SQL", "PL/pgSQL",
            "ACID", "ACID-транзакции", "ACID transactions",
            "Window functions", "CTE", "Stored procedures", "Triggers",
            "Replication", "Sharding", "Partitioning",
            "pgBouncer", "PgBouncer", "pgpool", "pgpool-II",
            "Barman", "pgBackRest", "WAL",
            "ANALYZE", "EXPLAIN", "Query optimization", "Query Optimization",
            "B-tree индексы", "Index optimization",
            "asyncpg", "psycopg2", "psycopg", "pg",
            "ADET", "ADH", "ArenaData", "Arenadata DB", "Arenadata_DB",
            "Arenadata Hyperwave",
            "RDS", "Aurora", "Cloud SQL", "Cloud Spanner",
            "phpMyAdmin",
        ],
        "starts": ["PostgreSQL", "MySQL", "SQL Server", "MS SQL"],
        "contains": [],
    },

    "NoSQL": {
        "exact": [
            "MongoDB", "Mongoose", "PyMongo", "MongoEngine",
            "Redis", "Redis Cluster", "RedisJSON", "RedisSearch",
            "Cassandra", "Apache Cassandra", "ScyllaDB", "Scylla",
            "HBase", "Apache HBase",
            "Aerospike", "AeroSpike",
            "Couchbase", "CouchDB",
            "DynamoDB", "Amazon DynamoDB",
            "Firestore",
            "Riak", "Voldemort",
            "Neo4j", "ArangoDB", "OrientDB",
            "InfluxDB", "TimescaleDB", "QuestDB",
            "Elasticsearch", "OpenSearch", "Solr", "Lucene",
            "Memcached",
        ],
        "starts": ["MongoDB", "Redis", "Cassandra", "Elasticsearch"],
        "contains": [],
    },

    "BI/Analytics": {
        "exact": [
            "ClickHouse", "ClickHouse Cloud",
            "Apache Spark", "Spark", "PySpark",
            "Trino", "Presto", "Hive", "Impala", "Apache Impala",
            "Druid", "Apache Druid",
            "Apache Iceberg", "Apache Ozone",
            "Apache Superset", "Superset",
            "dbt", "dbt Core", "dbt Cloud",
            "Apache Hudi", "Delta Lake",
            "Greenplum", "Vertica",
            "Power BI", "Tableau", "Looker", "Metabase", "Redash",
            "Apache Kafka Connect", "Kafka Connect",
            "OLAP", "ETL", "ELT", "Data Warehouse", "Data Lake", "Data Lakehouse",
            "Data Vault", "Anchor Modeling", "Dimensional Modeling",
        ],
        "starts": ["ClickHouse", "Apache Spark", "Spark", "dbt"],
        "contains": [],
    },

    "Миграции БД": {
        "domain_restrict": "DATABASE",
        "exact": [
            "Sqitch", "Atlas",
        ],
        "starts": [],
        "contains": ["migration", "миграция"],
    },

    "Vector DB": {
        "domain_restrict": "DATABASE",
        "exact": [
            "Chroma", "ChromaDB", "Qdrant", "Weaviate", "Pinecone",
            "Milvus", "Zilliz", "pgvector", "pg_vector",
            "Vespa", "Redis Vector", "OpenSearch Vector",
            "Marqo", "LanceDB", "Faiss", "FAISS",
            "Annoy", "HNSWlib",
            "Vector Database", "Vector DB", "Vector Search",
        ],
        "starts": ["Chroma", "Qdrant", "Weaviate", "Pinecone", "Milvus"],
        "contains": ["vector db", "vector database"],
    },

    # ══════════════════════════════════════════════════════════════
    # TESTING
    # ══════════════════════════════════════════════════════════════

    "Автотестирование": {
        "exact": [
            "Selenium", "Selenium WebDriver", "Selenium Grid",
            "Appium", "Playwright", "Cypress", "TestCafe", "Puppeteer",
            "WebdriverIO", "Nightwatch.js", "Nightwatch",
            "Selenoid", "Selenide",
            "PyTest", "pytest", "unittest",
            "TestNG", "JUnit 5",
            "Mocha", "Chai", "Jest", "Vitest",
            "Robot Framework", "Behave", "Cucumber",
            "Allure", "Allure Report", "Allure Framework",
            "Page Object Model", "POM", "Page Object",
            "Screenplay Pattern",
        ],
        "starts": ["Selenium", "Playwright", "Cypress", "Appium", "pytest"],
        "contains": [],
    },

    "Нагрузочное тестирование": {
        "exact": [
            "JMeter", "Apache JMeter",
            "Gatling", "k6", "Locust", "wrk", "Artillery",
            "NBomber", "Taurus", "BlazeMeter",
            "Vegeta", "autocannon", "hey",
            "Performance Testing", "Load Testing", "Stress Testing",
            "Нагрузочное тестирование", "Нагрузочное тестирование (JMeter)",
        ],
        "starts": ["JMeter", "Gatling", "k6"],
        "contains": [],
    },

    "API Testing": {
        "exact": [
            "Postman", "Postman Collections", "Newman",
            "SoapUI", "ReadyAPI",
            "REST Assured", "RestAssured",
            "Karate", "Karate DSL",
            "Pact", "Pactflow", "Contract Testing",
            "Insomnia", "Bruno", "Hoppscotch",
            "curl", "HTTPie",
        ],
        "starts": ["Postman", "REST Assured"],
        "contains": [],
    },

    "Тест-менеджмент": {
        "exact": [
            "TestRail", "Zephyr", "qTest", "TestLink", "TestIT",
            "Xray", "PractiTest", "Aqua",
            "TMS", "Test Management",
            "Jira", "Confluence", "YouTrack",
            "Bug Tracking", "Defect Management",
            "Test Plan", "Test Case", "Test Suite",
            "Чек-лист", "Тест-кейс", "Тест-план",
        ],
        "starts": ["TestRail", "TestIT"],
        "contains": [],
    },

    "Методологии тестирования": {
        "exact": [
            "TDD", "BDD", "ATDD", "DDD",
            "Shift-left Testing", "shift-left",
            "Exploratory Testing", "Exploratory testing",
            "Black Box", "White Box", "Gray Box",
            "Regression Testing", "Smoke Testing", "Sanity Testing",
            "Integration Testing", "Unit Testing", "E2E Testing",
            "A/B Testing",
            "ISTQB", "CTFL",
            "Тестирование", "Ручное тестирование",
        ],
        "starts": [],
        "contains": [],
    },

    # ══════════════════════════════════════════════════════════════
    # AI_ML / DATA_SCIENCE
    # ══════════════════════════════════════════════════════════════

    "ML/AI": {
        "exact": [
            "Machine Learning", "Scikit-learn", "sklearn",
            "NumPy", "Pandas", "SciPy", "Statsmodels",
            "XGBoost", "LightGBM", "CatBoost", "RandomForest",
            "Gradient Boosting", "Decision Tree",
            "Feature Engineering", "Feature Selection",
            "AutoML", "H2O", "TPOT", "Auto-sklearn",
            "MLflow", "Weights & Biases", "WandB",
            "Hugging Face", "Huggingface",
            "Recommendation Systems", "Recommender Systems",
            "A/B Testing",
            "Anomaly Detection", "Time Series",
            "Jupyter", "JupyterLab", "Jupyter Notebook",
            "Data Preprocessing", "Feature Store",
        ],
        "starts": ["scikit", "sklearn", "XGBoost", "LightGBM", "CatBoost"],
        "contains": [],
    },

    "Deep Learning": {
        "exact": [
            "PyTorch", "TensorFlow", "Keras", "JAX",
            "ONNX", "ONNX Runtime",
            "CUDA", "CuDNN", "TensorRT",
            "torchvision", "torchaudio", "torchtext",
            "PyTorch Lightning", "FastAI", "fast.ai",
            "Distributed Training", "Horovod",
            "Neural Network", "CNN", "RNN", "LSTM", "Transformer",
            "Attention Mechanism", "Self-Attention",
            "Diffusion Models", "GANs", "VAE",
        ],
        "starts": ["PyTorch", "TensorFlow", "Keras"],
        "contains": [],
    },

    "NLP": {
        "exact": [
            "BERT", "GPT", "GPT-4", "GPT-3",
            "LangChain", "LlamaIndex", "Llama Index",
            "spaCy", "NLTK", "Gensim",
            "Transformers", "Hugging Face Transformers",
            "Sentence Transformers", "SentenceTransformers",
            "OpenAI API", "OpenAI",
            "Ollama", "LLM", "LLMs",
            "RAG", "Retrieval Augmented Generation",
            "Tokenization", "Embedding", "Embeddings",
            "NLP", "Natural Language Processing",
            "Text Classification", "Named Entity Recognition", "NER",
            "Sentiment Analysis", "Summarization",
        ],
        "starts": ["LangChain", "spaCy", "BERT", "GPT"],
        "contains": [],
    },

    "Computer Vision": {
        "exact": [
            "OpenCV", "cv2",
            "YOLO", "YOLOv5", "YOLOv8", "YOLOv9",
            "Detectron2", "torchvision",
            "PIL", "Pillow",
            "Image Segmentation", "Object Detection", "Image Classification",
            "Face Recognition", "Face Detection",
            "Stable Diffusion", "ControlNet", "ComfyUI",
            "MediaPipe",
            "Computer Vision",
        ],
        "starts": ["OpenCV", "YOLO"],
        "contains": [],
    },

    "MLOps": {
        "exact": [
            "MLflow", "MLflow Tracking", "MLflow Projects",
            "Kubeflow", "Kubeflow Pipelines",
            "Apache Airflow", "Airflow", "Prefect", "Dagster", "Luigi",
            "DVC", "Git LFS",
            "BentoML", "Triton Inference Server", "Seldon", "KServe",
            "Evidently", "Great Expectations",
            "Feature Store", "Feast",
            "Model Registry", "Model Serving",
            "ML Pipeline", "Data Pipeline",
        ],
        "starts": ["MLflow", "Airflow", "Kubeflow"],
        "contains": [],
    },

    "Data Engineering": {
        "exact": [
            "Apache Kafka", "Kafka", "Apache Flink", "Flink",
            "Apache Beam", "Beam",
            "Apache NiFi", "NiFi",
            "Debezium", "CDC",
            "dbt", "dbt Core",
            "Airbyte", "Fivetran", "Stitch",
            "Snowflake", "BigQuery", "Redshift",
            "Data Engineering", "Data Pipeline", "ETL", "ELT",
            "Stream Processing", "Batch Processing",
            "Apache Hadoop", "Hadoop", "HDFS", "YARN", "MapReduce",
            "Hive", "HiveQL", "Pig", "Oozie",
            "Azure Data Factory", "AWS Glue", "Google Dataflow",
        ],
        "starts": ["Apache Kafka", "Apache Flink", "Apache Beam", "Kafka"],
        "contains": [],
    },

    # ══════════════════════════════════════════════════════════════
    # CLOUD
    # ══════════════════════════════════════════════════════════════

    "AWS": {
        "domain_restrict": "CLOUD",
        "exact": [
            "AWS", "Amazon Web Services",
            "EC2", "S3", "Lambda", "ECS", "EKS", "ECR",
            "RDS", "Aurora", "DynamoDB", "ElastiCache",
            "SQS", "SNS", "EventBridge", "Kinesis",
            "CloudFormation", "AWS CDK", "SAM",
            "IAM", "Cognito", "AWS WAF",
            "CloudWatch", "X-Ray", "CloudTrail",
            "Route 53", "CloudFront", "ALB", "ELB", "NLB",
            "VPC", "Direct Connect", "Transit Gateway",
            "Glacier", "EFS", "EBS",
            "Fargate", "Lightsail", "Elastic Beanstalk",
            "Step Functions", "API Gateway", "AppSync",
            "Athena", "EMR", "Glue", "Redshift",
            "SageMaker", "Rekognition", "Polly", "Lex",
        ],
        "starts": ["AWS ", "Amazon "],
        "contains": [],
    },

    "GCP": {
        "domain_restrict": "CLOUD",
        "exact": [
            "GCP", "Google Cloud", "Google Cloud Platform",
            "GKE", "Cloud Run", "Cloud Functions", "App Engine",
            "BigQuery", "Cloud Storage", "Cloud SQL", "Cloud Spanner",
            "Firestore", "Firebase", "Pub/Sub",
            "Dataflow", "Dataproc", "Composer",
            "Cloud Build", "Cloud Deploy", "Artifact Registry",
            "Cloud Monitoring", "Cloud Logging", "Cloud Trace",
            "Cloud IAM", "Secret Manager",
            "Cloud CDN", "Cloud Load Balancing",
            "Vertex AI", "AutoML", "Cloud Vision API",
        ],
        "starts": ["GCP", "Google Cloud", "GKE"],
        "contains": [],
    },

    "Azure": {
        "exact": [
            "Azure", "Microsoft Azure",
            "Azure DevOps", "Azure Pipelines", "Azure Repos",
            "Azure Kubernetes Service", "AKS",
            "Azure Functions", "Azure App Service", "Azure Container Apps",
            "Azure SQL", "Azure Cosmos DB", "Cosmos DB",
            "Azure Blob Storage", "Azure Data Lake",
            "Azure Service Bus", "Azure Event Hub", "Azure Event Grid",
            "Azure Monitor", "Application Insights", "Log Analytics",
            "Azure Active Directory", "Azure AD", "Entra ID",
            "Azure Key Vault", "Azure Policy",
            "Azure API Management",
            "Azure Machine Learning", "Azure Synapse",
        ],
        "starts": ["Azure ", "AKS"],
        "contains": [],
    },

    "Cloud": {
        "exact": [
            "IaaS", "PaaS", "SaaS", "FaaS",
            "Serverless", "Serverless Framework",
            "Multi-cloud", "Hybrid Cloud", "Private Cloud",
            "CDN", "Edge Computing",
            "Cloud Native", "Cloud-Native",
            "FinOps", "Cloud Cost Optimization",
            "Cloud Security", "Zero Trust",
            "Service Mesh",
            "Load Balancing", "Auto Scaling",
            "Disaster Recovery", "High Availability", "HA",
            "SLA", "SLO", "SLI",
            "Yandex Cloud", "VK Cloud", "SberCloud",
            "Mail.ru Cloud", "MTS Cloud",
            "OpenStack", "VMware vSphere", "VMware", "vSphere",
            "Proxmox", "oVirt", "KVM",
        ],
        "starts": ["Serverless", "Cloud"],
        "contains": [],
    },

    # ══════════════════════════════════════════════════════════════
    # SECURITY
    # ══════════════════════════════════════════════════════════════

    "Пентестирование": {
        "exact": [
            "Burp Suite", "Metasploit", "Kali Linux",
            "nmap", "Nmap", "Wireshark", "Tshark",
            "OWASP ZAP", "Nikto", "Nessus", "OpenVAS",
            "SQLMap", "Hydra", "Medusa", "John the Ripper", "Hashcat",
            "Aircrack-ng", "Bettercap",
            "Cobalt Strike", "Empire",
            "Penetration Testing", "Pentesting", "Red Team",
            "OWASP Top 10", "OWASP",
            "CVE", "CVSS", "Vulnerability Assessment",
            "Bug Bounty",
        ],
        "starts": ["Burp Suite", "Metasploit"],
        "contains": [],
    },

    "Криптография": {
        "exact": [
            "PKI", "TLS", "SSL", "mTLS", "TLS/SSL",
            "OpenSSL", "LibreSSL", "BouncyCastle",
            "HSM", "TPM",
            "PGP", "GPG",
            "AES", "RSA", "ECC", "SHA", "HMAC",
            "Certificate", "X.509", "CSR",
            "Cryptography", "Encryption", "Hashing",
            "KMS", "AWS KMS", "Azure Key Vault",
            "Vault", "HashiCorp Vault",
        ],
        "starts": ["TLS", "PKI"],
        "contains": [],
    },

    "IAM": {
        "exact": [
            "OAuth2", "OAuth 2.0", "OIDC", "OpenID Connect",
            "SAML", "SAML 2.0",
            "JWT", "JSON Web Token",
            "Keycloak", "Okta", "Auth0",
            "Active Directory", "AD", "LDAP", "LDAPS",
            "RBAC", "ABAC", "DAC", "MAC",
            "SSO", "Single Sign-On", "MFA", "2FA",
            "PAM", "CyberArk", "BeyondTrust",
            "IAM", "Identity Management", "Access Management",
        ],
        "starts": ["OAuth", "Keycloak", "LDAP"],
        "contains": [],
    },

    "Compliance": {
        "exact": [
            "ISO 27001", "ISO 27002", "ISO 27005",
            "SOC 2", "SOC2",
            "PCI DSS",
            "GDPR",
            "HIPAA",
            "FSTEC", "ФСБ", "ФЗ-152", "152-ФЗ",
            "ГОСТ Р", "ГОСТ ИСО",
            "NIST", "CIS Benchmarks",
            "Audit", "Compliance", "GRC",
            "Risk Management", "Risk Assessment",
            "DLP", "SIEM", "SOC",
            "Security Policy", "Security Awareness",
        ],
        "starts": ["ISO 27", "PCI DSS", "GDPR"],
        "contains": [],
    },

    # ══════════════════════════════════════════════════════════════
    # MOBILE
    # ══════════════════════════════════════════════════════════════

    "iOS": {
        "exact": [
            "iOS", "Swift", "SwiftUI", "UIKit",
            "Objective-C",
            "CoreData", "Core Data", "CoreML", "Core ML",
            "ARKit", "RealityKit", "SceneKit",
            "XCTest", "XCUITest",
            "CocoaPods", "Swift Package Manager", "SPM",
            "Xcode", "Instruments",
            "Push Notifications", "APNs",
            "HealthKit", "MapKit", "StoreKit",
            "App Store", "TestFlight",
            "Combine", "Concurrency", "async/await",
        ],
        "starts": ["Swift", "SwiftUI", "UIKit"],
        "contains": [],
    },

    "Android": {
        "exact": [
            "Android", "Kotlin", "Jetpack Compose",
            "Java (Android)", "Android SDK",
            "Room", "Room Database",
            "Retrofit", "OkHttp",
            "Coroutines", "Kotlin Coroutines", "Flow",
            "Dagger", "Dagger 2", "Hilt",
            "Navigation Component", "ViewModel", "LiveData",
            "WorkManager", "DataStore",
            "Glide", "Picasso", "Coil",
            "Android Studio", "Espresso", "Robolectric",
            "Firebase Cloud Messaging", "FCM",
            "Google Play", "Play Store",
            "Android Architecture Components",
        ],
        "starts": ["Android", "Jetpack", "Dagger"],
        "contains": [],
    },

    "Cross-platform": {
        "exact": [
            "React Native", "Flutter", "Dart",
            "Xamarin", "Xamarin.Forms", "MAUI",
            "Ionic", "Capacitor", "Cordova",
            "NativeScript",
            "Expo", "Expo Go",
            "KMM", "Kotlin Multiplatform", "Kotlin Multiplatform Mobile",
            "PWA", "Progressive Web App",
        ],
        "starts": ["React Native", "Flutter", "Xamarin"],
        "contains": [],
    },

    # ══════════════════════════════════════════════════════════════
    # IOT / SYSTEMS
    # ══════════════════════════════════════════════════════════════

    "Протоколы IoT": {
        "domain_restrict": "IOT",
        "exact": [
            "MQTT", "MQTT Broker", "Mosquitto",
            "CoAP", "AMQP",
            "Modbus", "Modbus TCP", "Modbus RTU",
            "OPC-UA", "OPC UA",
            "Zigbee", "Z-Wave",
            "LoRa", "LoRaWAN",
            "BLE", "Bluetooth Low Energy", "Bluetooth",
            "NFC", "RFID",
            "6LoWPAN", "Thread", "Matter",
            "CAN", "CAN Bus", "LIN", "LIN Bus",
            "Profibus", "Profinet",
        ],
        "starts": ["MQTT", "Modbus", "OPC"],
        "contains": [],
    },

    "Микроконтроллеры": {
        "domain_restrict": "IOT",
        "exact": [
            "Arduino", "ESP32", "ESP8266",
            "STM32", "STM32CubeIDE",
            "Raspberry Pi", "RPi",
            "RTOS", "FreeRTOS", "Zephyr RTOS", "Mbed OS",
            "AVR", "PIC", "ARM Cortex",
            "FPGA", "Verilog", "VHDL",
            "Embedded C", "C (embedded)", "bare metal",
            "Yocto", "Buildroot", "OpenWRT",
            "Linux Embedded", "Embedded Linux",
            "HAL", "BSP",
        ],
        "starts": ["STM32", "ESP32", "Arduino", "FreeRTOS"],
        "contains": [],
    },

    "Системное ПО": {
        "domain_restrict": "SYSTEMS",
        "exact": [
            "Linux", "Ubuntu", "Debian", "CentOS", "RHEL", "Fedora",
            "Arch Linux", "Alpine Linux",
            "Windows Server", "Active Directory",
            "systemd", "init",
            "POSIX", "UNIX",
            "Shell", "Bash", "sh", "zsh", "fish",
            "PowerShell",
            "Networking", "TCP/IP", "OSI model",
            "DNS", "DHCP", "HTTP", "HTTPS", "FTP", "SSH",
            "Firewall", "iptables", "nftables", "pfSense",
            "Virtualization", "KVM", "QEMU",
            "NFS", "SMB", "CIFS", "iSCSI",
            "C", "C++", "C/C++",
            "Assembly", "Assembler",
        ],
        "starts": ["Linux", "Ubuntu", "Debian", "CentOS", "RHEL", "Bash", "Shell"],
        "contains": [],
    },

    # ══════════════════════════════════════════════════════════════
    # FRONTEND
    # ══════════════════════════════════════════════════════════════

    "JavaScript": {
        "exact": [
            "React", "React.js", "Next.js", "Next", "Gatsby",
            "Vue", "Vue.js", "Vue 3", "Vue 2", "Nuxt.js", "Nuxt", "Nuxt 3",
            "Angular", "AngularJS",
            "Svelte", "SvelteKit", "Solid.js", "SolidJS",
            "Astro", "Remix", "Qwik",
            "Redux", "MobX", "Vuex", "Pinia", "Zustand", "Jotai", "Recoil",
            "RxJS", "NgRx",
            "React Router", "Vue Router", "vue-router", "Vue Router",
            "Webpack", "Vite", "Rollup", "Parcel", "esbuild", "Turbopack",
            "Babel", "TypeScript",
            "ESLint", "Prettier", "Husky",
            "Jest", "Vitest", "Mocha", "Chai", "Testing Library",
            "Storybook", "Chromatic",
            "Cypress", "Playwright", "Puppeteer",
            "GraphQL", "Apollo Client", "urql",
            "React Query", "TanStack Query", "SWR",
            "Socket.io (client)", "WebSocket API",
            "Alpine.js", "AlpineJS", "HTMX", "Stimulus",
            "D3.js", "D3", "Chart.js", "Recharts", "ECharts",
            "Three.js", "Three",
            "Lodash", "Underscore.js", "Ramda",
            "Axios", "Fetch API",
            "npm", "yarn", "pnpm",
            "jQuery",
            "Emotion", "styled-components", "CSS-in-JS",
        ],
        "starts": ["React", "Vue", "Angular", "Next.js", "Nuxt", "Svelte"],
        "contains": [],
    },

    "HTML/CSS": {
        "exact": [
            "HTML", "HTML5",
            "CSS", "CSS3",
            "Sass", "SCSS", "Less", "Stylus", "PostCSS",
            "Tailwind", "Tailwind CSS", "TailwindCSS",
            "Bootstrap", "Bulma", "Foundation",
            "Material UI", "Material Design", "MUI",
            "Ant Design", "Semantic UI",
            "Chakra UI", "Mantine", "Radix UI", "shadcn/ui",
            "BEM", "OOCSS", "SMACSS",
            "Flexbox", "Grid", "CSS Grid", "CSS Flexbox",
            "CSS Animations", "CSS Transitions",
            "CSS Variables", "CSS Custom Properties",
            "CSS Modules",
            "Responsive Design", "Adaptive Design", "Mobile First",
            "Верстка", "Адаптивная верстка", "Кроссбраузерная верстка",
            "Web Components", "Shadow DOM", "Custom Elements",
            "ARIA", "a11y", "Accessibility (a11y)", "accessibility",
            "Web Standards", "W3C",
        ],
        "starts": ["Tailwind", "Bootstrap", "Sass", "SCSS"],
        "contains": [],
    },

    "Дизайн": {
        "exact": [
            "Figma", "Sketch", "Adobe XD", "InVision",
            "Zeplin", "Avocode",
            "Adobe Photoshop", "Photoshop",
            "Adobe Illustrator", "Illustrator",
            "Framer", "Principle", "ProtoPie",
            "UI/UX", "UX Design", "UI Design",
            "UX Research", "User Research",
            "Wireframing", "Prototyping",
            "Design System", "Design Systems",
            "Atomic Design",
        ],
        "starts": ["Figma", "Adobe"],
        "contains": [],
    },

    # ══════════════════════════════════════════════════════════════
    # GENERAL: методологии, архитектура, soft skills
    # ══════════════════════════════════════════════════════════════

    "Методологии": {
        "exact": [
            "Agile", "Scrum", "Kanban", "SAFe", "LeSS", "Nexus",
            "Lean", "Lean Development",
            "Waterfall", "RUP", "PRINCE2",
            "XP", "Extreme Programming",
            "OKR", "KPI",
            "Retrospective", "Sprint", "Backlog", "Velocity",
            "Continuous Improvement", "Kaizen",
            "PMBOK", "PMP",
            "ITIL", "ITSM",
        ],
        "starts": ["Agile", "Scrum", "SAFe"],
        "contains": [],
    },

    "Архитектура": {
        "exact": [
            "Microservices", "Microservice Architecture",
            "Monolith", "Monolithic Architecture",
            "Event-Driven Architecture", "EDA",
            "CQRS", "Event Sourcing",
            "DDD", "Domain-Driven Design",
            "Clean Architecture", "Hexagonal Architecture", "Onion Architecture",
            "SOLID", "GRASP", "Design Patterns", "Паттерны проектирования",
            "SOA", "Service-Oriented Architecture",
            "API Gateway Pattern", "Saga Pattern", "Outbox Pattern",
            "Load Balancing", "Circuit Breaker", "Bulkhead",
            "CAP Theorem", "BASE", "ACID",
            "12-factor app", "12-factor apps",
            "High Load", "Highload", "Scalability",
            "Distributed Systems", "Distributed Computing",
        ],
        "starts": ["Microservice", "Domain-Driven", "Clean Architecture"],
        "contains": [],
    },

    "VCS": {
        "exact": [
            "Git", "GitHub", "GitLab", "Bitbucket", "Azure Repos",
            "SVN", "Subversion", "Mercurial",
            "Git Flow", "GitFlow", "Trunk-Based Development",
            "Code Review", "Pull Request", "Merge Request",
            "Branching Strategy", "Git branching",
            "Conventional Commits", "Semantic Versioning", "SemVer",
        ],
        "starts": ["Git"],
        "contains": [],
    },

    "Message Brokers": {
        "exact": [
            "Apache Kafka", "Kafka", "Kafka Streams",
            "RabbitMQ", "AMQP",
            "NATS", "NATS Streaming", "JetStream",
            "ActiveMQ", "Artemis MQ", "ArtemisMQ",
            "Azure Service Bus",
            "AWS SQS", "AWS SNS", "Amazon SQS",
            "Google Pub/Sub",
            "Redis Streams",
            "ZeroMQ", "nanomsg",
            "Message Queue", "Message Broker",
            "Event Streaming", "Event Bus",
        ],
        "starts": ["Kafka", "RabbitMQ", "NATS"],
        "contains": [],
    },

    "Принципы": {
        "exact": [
            "SOLID", "DRY", "KISS", "YAGNI",
            "OOP", "ООП", "Functional Programming", "FP",
            "Reactive Programming",
            "Design Patterns", "GoF Patterns",
            "Clean Code", "Refactoring",
            "GRASP",
            "Object-Oriented Programming", "Объектно-ориентированное программирование",
        ],
        "starts": [],
        "contains": [],
    },

    "Аналитика/BI": {
        "exact": [
            "Power BI", "Power BI Desktop", "Power BI Service",
            "Tableau", "Tableau Desktop", "Tableau Server",
            "Looker", "Looker Studio", "Google Data Studio",
            "Metabase", "Redash", "Apache Superset", "Superset",
            "Excel", "Microsoft Excel", "Google Sheets",
            "SQL Analytics", "Analytical SQL",
            "Data Visualization", "Визуализация данных",
            "Dashboard", "Dashboards", "Reporting",
            "Business Intelligence", "BI",
            "Qlik", "QlikView", "Qlik Sense",
            "MicroStrategy", "SAP Business Objects",
            "Yandex DataLens", "DataLens",
        ],
        "starts": ["Power BI", "Tableau", "Looker"],
        "contains": [],
    },

    "Soft Skills": {
        "exact": [
            "Communication", "Коммуникация", "Коммуникативные навыки",
            "Teamwork", "Team Work", "Работа в команде",
            "Leadership", "Лидерство",
            "Problem Solving", "Решение проблем",
            "Critical Thinking", "Критическое мышление",
            "Time Management", "Управление временем",
            "Presentation", "Презентация",
            "Mentoring", "Наставничество",
            "English", "Английский язык", "Technical English",
            "B1", "B2", "C1", "IELTS", "TOEFL",
            "Writing", "Technical Writing", "Документирование",
        ],
        "starts": [],
        "contains": [],
    },

    "3D/Графика": {
        "exact": [
            "3D Max", "3ds Max", "3DS Max",
            "Blender", "Maya", "Cinema 4D",
            "ZBrush", "Substance Painter", "Substance Designer",
            "3D Modeling", "3D-моделирование", "3D моделирование",
            "3D-модели", "3D графика и рендеринг",
            "3D", "2D", "3D Software",
            "Unreal Engine", "Unity", "Godot",
            "OpenGL", "WebGL", "DirectX", "Vulkan", "Metal",
            "Rendering", "Ray Tracing", "PBR",
        ],
        "starts": ["3D", "Unreal", "Unity", "Blender"],
        "contains": [],
    },
}


# ─── Утилиты ─────────────────────────────────────────────────────────────────

def build_where_clause(rules: dict, param_offset: int = 1):
    """Строит SQL WHERE условие и список параметров для одного семейства."""
    conditions = []
    params = []
    idx = param_offset

    for name in rules.get("exact", []):
        conditions.append(f"LOWER(sc.name) = LOWER(%s)")
        params.append(name)
        idx += 1

    for prefix in rules.get("starts", []):
        conditions.append(f"LOWER(sc.name) LIKE LOWER(%s)")
        params.append(prefix.rstrip("%") + "%")
        idx += 1

    for sub in rules.get("contains", []):
        conditions.append(f"LOWER(sc.name) LIKE LOWER(%s)")
        params.append("%" + sub.strip("%") + "%")
        idx += 1

    return conditions, params


def preview_family(cur, family: str, rules: dict, domain_filter: str | None):
    conditions, params = build_where_clause(rules)
    if not conditions:
        return []

    # domain_restrict in rules overrides the CLI --domain filter
    effective_domain = rules.get("domain_restrict") or domain_filter
    domain_clause = ""
    if effective_domain:
        domain_clause = f" AND sc.domain = %s"
        params.append(effective_domain)

    sql = f"""
        SELECT sc.id, sc.name, sc.domain, sc.tech_family
        FROM skill_canonical sc
        WHERE ({' OR '.join(conditions)}){domain_clause}
        ORDER BY sc.domain, sc.name
    """
    cur.execute(sql, params)
    return cur.fetchall()


def apply_family(cur, family: str, rules: dict, domain_filter: str | None) -> int:
    conditions, where_params = build_where_clause(rules)
    if not conditions:
        return 0

    # domain_restrict in rules overrides the CLI --domain filter
    effective_domain = rules.get("domain_restrict") or domain_filter
    domain_clause = ""
    if effective_domain:
        domain_clause = f" AND sc.domain = %s"
        where_params.append(effective_domain)

    sql = f"""
        UPDATE skill_canonical sc
        SET tech_family = %s
        WHERE ({' OR '.join(conditions)}){domain_clause}
    """
    # family идёт ПЕРВЫМ (для SET), затем условия WHERE
    all_params = [family] + where_params
    cur.execute(sql, all_params)
    return cur.rowcount


def show_unknown(cur, domain_filter: str | None):
    domain_clause = ""
    params = []
    if domain_filter:
        domain_clause = "AND domain = %s"
        params.append(domain_filter)

    cur.execute(f"""
        SELECT id, name, domain, version_group
        FROM skill_canonical
        WHERE tech_family IS NULL {domain_clause}
        ORDER BY domain, name
        LIMIT 200
    """, params)
    rows = cur.fetchall()
    print(f"\n{'ID':>8}  {'Домен':<15}  {'version_group':<20}  name")
    print("─" * 80)
    for row in rows:
        sid, name, dom, vg = row
        print(f"{sid:>8}  {(dom or '—'):<15}  {(vg or '—'):<20}  {name}")
    print(f"\nИтого: {len(rows)} навыков без tech_family (лимит 200)")


# ─── Main ─────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Заполнить tech_family в skill_canonical (rule-based)")
    parser.add_argument("--save",         action="store_true",  help="Применить изменения (иначе только preview)")
    parser.add_argument("--family",       default=None,         help="Обработать только одно семейство")
    parser.add_argument("--domain",       default=None,         help="Ограничить доменом (BACKEND, FRONTEND, ...)")
    parser.add_argument("--show-unknown", action="store_true",  help="Показать навыки без tech_family")
    args = parser.parse_args()

    if not DB_URL:
        sys.exit("[ERROR] DB_URL не задан в .env")

    import psycopg2
    conn = psycopg2.connect(DB_URL)
    cur  = conn.cursor()

    if args.show_unknown:
        show_unknown(cur, args.domain)
        cur.close()
        conn.close()
        return

    families_to_process = (
        {args.family: RULES[args.family]} if args.family and args.family in RULES
        else RULES
    )

    if args.family and args.family not in RULES:
        sys.exit(f"[ERROR] Семейство '{args.family}' не найдено. Доступны: {list(RULES.keys())}")

    total_updated = 0

    for family, rules in families_to_process.items():
        rows = preview_family(cur, family, rules, args.domain)

        already    = [r for r in rows if r[3] == family]
        to_update  = [r for r in rows if r[3] != family]
        other_fam  = [r for r in to_update if r[3] is not None]
        new_assign = [r for r in to_update if r[3] is None]

        print(f"\n{'═'*70}")
        print(f"  Семейство: {family}")
        print(f"  Совпадений по правилам: {len(rows)}")
        print(f"  ├─ уже имеют tech_family='{family}': {len(already)}")
        print(f"  ├─ будут назначены (tech_family IS NULL): {len(new_assign)}")
        print(f"  └─ перезапись другого значения: {len(other_fam)}")

        if to_update:
            print(f"\n  {'ID':>8}  {'Домен':<15}  {'Старый tech_family':<20}  name")
            print(f"  {'─'*65}")
            for sid, name, dom, old_fam in to_update[:50]:
                action = "→ assign" if old_fam is None else f"→ overwrite '{old_fam}'"
                print(f"  {sid:>8}  {(dom or '—'):<15}  {(old_fam or '—'):<20}  {name}  [{action}]")
            if len(to_update) > 50:
                print(f"  ... ещё {len(to_update) - 50} строк")

        if args.save and to_update:
            n = apply_family(cur, family, rules, args.domain)
            conn.commit()
            total_updated += n
            print(f"\n  ✅ Обновлено: {n} строк")
        elif not args.save and to_update:
            print(f"\n  ℹ️  Запустите с --save чтобы применить")

    if args.save:
        print(f"\n{'═'*70}")
        print(f"Итого обновлено: {total_updated} строк")
    else:
        print(f"\n{'═'*70}")
        print("Preview-режим. Для применения добавьте --save")

    cur.close()
    conn.close()


if __name__ == "__main__":
    main()
