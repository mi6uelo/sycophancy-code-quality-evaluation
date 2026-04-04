🧪 Sycophancy Code Quality Benchmark
📌 Descripción general

Este repositorio presenta un benchmark experimental diseñado para evaluar el impacto del sycophancy bias (sesgo de complacencia) en modelos de lenguaje de gran tamaño (LLMs) sobre la calidad técnica del código generado en entornos de desarrollo asistido por inteligencia artificial.

El estudio analiza cómo los modelos, al recibir instrucciones sesgadas o sugerentes, pueden priorizar la concordancia con el usuario por encima de la corrección técnica, afectando negativamente la calidad del software.

🎯 Objetivo

Evaluar experimentalmente si el comportamiento complaciente de los LLMs produce una degradación medible en la calidad del código, utilizando métricas de análisis estático.

🧠 Contexto de investigación

La creciente adopción de herramientas de desarrollo asistidas por IA (como ChatGPT o asistentes tipo Copilot) introduce un riesgo relevante:

Los modelos pueden validar supuestos incorrectos del desarrollador, reforzando implementaciones defectuosas en lugar de corregirlas.

Este fenómeno, conocido como sycophancy bias, puede derivar en:

Incremento de deuda técnica
Reducción de mantenibilidad
Introducción de vulnerabilidades
Decisiones arquitectónicas subóptimas
🧪 Diseño experimental
🔹 Enfoque
Diseño experimental controlado
Evaluación basada en escenarios
Comparación entre:
Prompts neutrales
Prompts con sesgo de complacencia
🔹 Tipos de tareas
Operaciones CRUD
Implementaciones de APIs REST
🔹 Lenguaje de programación
Java
🔹 Modelos evaluados
GPT-5.4
Claude Opus 4.6
Gemini 3.1 Pro
Grok Code Fast 1
📊 Métricas de evaluación

La calidad del código se evalúa mediante SonarQube / SonarLint:

Code Smells
Bugs
Vulnerabilities
Complejidad cognitiva
Índice de mantenibilidad
Deuda técnica
``` 
Estructura del repositorio
.
├── scenarios/              # Escenarios experimentales (CRUD / API)
├── prompts/                # Prompts neutrales y complacientes
├── models/                 # Código generado por cada modelo
├── results/                # Resultados procesados y comparaciones
├── sonarqube-reports/      # Reportes de análisis estático
├── scripts/                # Scripts de automatización
└── docs/                   # Documentación del estudio
```
🔁 Procedimiento experimental
Definir un escenario de desarrollo (CRUD o API)
Generar código utilizando:
Prompt neutral
Prompt con sesgo de complacencia
Repetir el proceso para cada modelo
Analizar el código con SonarQube
Extraer métricas de calidad
Comparar resultados entre condiciones
📈 Resultados esperados

Se espera evidenciar que:

Los prompts complacientes reducen la calidad del código
Los modelos refuerzan supuestos incorrectos del usuario
Se incrementan:
Code smells
Deuda técnica
Complejidad estructural
📊 Análisis de datos
Estadística descriptiva
Análisis comparativo entre condiciones
Pruebas estadísticas:
t-test
Mann-Whitney U
⚠️ Limitaciones
Número limitado de modelos evaluados
Evaluación basada en escenarios (no sistemas completos)
El análisis estático no captura errores en tiempo de ejecución
🔬 Reproducibilidad

Este repositorio está diseñado para garantizar la reproducibilidad del estudio:

Prompts incluidos
Escenarios estandarizados
Resultados versionados
Scripts de automatización disponibles
📚 Palabras clave

Sycophancy Bias, Validación complaciente, Calidad del código, Deuda técnica, SonarQube, Mantenibilidad del software, Evaluación experimental, LLMs

👨‍💻 Autor

Miguel Guevara
Investigador en Ingeniería de Software

📄 Licencia

Este proyecto se publica con fines académicos y de investigación.

🚀 Contribuciones

Se aceptan contribuciones en:

Nuevos escenarios
Inclusión de modelos adicionales
Mejora de métricas de evaluación
Estudios de replicación

📢 Citación sugerida
@article{guevara2026sycophancy,
  title={Evaluación del impacto del sycophancy bias en la calidad del código en entornos de desarrollo asistido por IA},
  author={Guevara, Miguel},
  year={2026}
}
