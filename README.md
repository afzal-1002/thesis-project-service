# 📘 Bug Resolution Time Estimation – Microservices Platform  
**Spring Boot • AI • Jira Integration • Microservices Architecture**

This repository contains the implementation of my Master’s Thesis project titled:  
**“Enhancing Bug Resolution Time Estimation with AI in Spring Boot Microservices.”**

The system uses a modular microservices architecture built with Spring Boot, integrated with Jira APIs, and complemented by AI/NLP models to estimate bug resolution time for software issues.

---

## 🏗️ System Overview

The project is structured as a distributed **Spring Boot microservices architecture**, consisting of:

- **API Gateway** – Routing and central entry point  
- **Eureka Server** – Service discovery and registry  
- **User-Service** – User authentication and management  
- **Project-Service** – Jira project metadata, versions, and components  
- **Issue-Service** – Fetching Jira issues and processing data  
- **Comment-Service** – AI-powered comment generation and analysis  
- **Estimation-Service** – AI/NLP processing, feature engineering, and time estimation  
- **Notification-Service** – Sending notifications or updates  
- **Audit-Service** – Logging, traceability, and system monitoring  

All services communicate using **Feign Client**, registered via **Eureka**, and routed through the **API Gateway**.

---

## 🔗 Architecture Diagram  
*(Will be added soon)*  
System architecture diagrams (Sequence Diagram, Microservices Overview, Data Flow Diagram) are being prepared and will be included later.

---

## 🤖 AI & NLP Components

The system integrates AI features to estimate bug resolution time:

- Text preprocessing and NLP feature extraction  
- AI-driven estimation using DeepSeek/GPT-style models  
- Jira comment analysis and automatic structured comment generation  
- Predictive modelling based on historical issue data  

---

## 🧩 Features

- Fetches live issue and project data from Jira  
- Converts monolithic logic into fully distributed microservices  
- Uses Feign Client for internal communication  
- Implements fault tolerance and load balancing through Eureka  
- Structured AI comment generation  
- Jira issue status, project, and comment synchronization  
- Clean, modular, and scalable architecture  

---

## 📁 Repository Structure

```
root/
│
├── API-GATEWAY/
├── SERVICE-REGISTRY/
│
├── USER-SERVICE/
├── AI-INTEGRATION
├── PROJECT-SERVICE/
├── ISSUES-SERVICE/
│
└── README.md
```

---

## 🛠️ Technologies Used

- **Java 17+**
- **Spring Boot 3+**
- **Spring Cloud (Eureka, Gateway, OpenFeign)**
- **MySQL**
- **Jira REST API**
- **Docker (optional)**
- **AI Model Integration (DeepSeek/OpenAI)**

---

## 🧪 Work in Progress

- Architecture diagrams  
- Model training scripts  
- Final dataset integration  
- Visualization dashboards  

Updates will be added throughout the thesis development.

---

## 📬 Contact

**Author:** Muhammad Afzal  
**University:** Warsaw University of Technology  
**Supervisor:** doc. dr inż. Roman Podraza  

If you have feedback or suggestions, feel free to open an issue or contact me.
