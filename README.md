# 📘 YouTube DSA Summariser – AI Powered Revision Generator

This project is an end-to-end **YouTube DSA Lecture Summariser** built using **Angular**, **Spring Boot**, and **Gemini API**.

It automatically extracts knowledge from long DSA lectures and generates **clean, exam-ready revision notes** including algorithm explanation, pseudocode, complexity analysis, edge cases, and revision points.

---

## ✨ Features

- AI-powered **DSA concept extraction** from YouTube videos  
- Smart **semantic chunking & priority filtering**  
- Single-call **Gemini summarization** (quota-optimized)  
- Structured output:  
  - Problem Statement  
  - Algorithm Steps  
  - Pseudocode  
  - Time & Space Complexity  
  - Edge Cases  
  - 5-Line Revision Notes  
- **PDF export** of generated notes  
- Full-stack architecture with REST API  

---

## 🧠 Workflow

1. User submits a YouTube lecture link  
2. Backend extracts transcript / metadata / comments  
3. Transcript is chunked & most informative parts are selected  
4. All content is merged and sent in **one Gemini request**  
5. Gemini returns structured DSA revision notes  
6. Notes are displayed on frontend and exported as PDF  

---

## ⚙️ Tech Stack

| Layer      | Technology |
|-----------|-----------|
| Frontend  | Angular, TypeScript, HTML, CSS |
| Backend   | Spring Boot, Java |
| AI Engine | Gemini API |
| Build     | Maven |
| PDF Tool  | jsPDF |

---

## 📂 Project Structure
```
youtube-dsa-summarizer/
│
├── backend/
│ ├── controller/
│ ├── service/
│ ├── model/
│ └── exception/
│
└── frontend/
├── components/
├── services/
└── models/
```

---

## 🚀 How to Run

### Backend

```bash
cd backend
mvn spring-boot:run
```


---

### Frontend

```bash
cd frontend
npm install
ng serve
```

---

## 📄 Sample Output

The generated revision notes contain:

- Clear algorithm explanation  
- Optimized approach  
- Interview-ready pseudocode  
- Accurate time & space complexity  
- Key edge cases  
- Quick 5-line revision notes  
