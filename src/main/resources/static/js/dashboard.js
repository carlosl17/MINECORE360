// ==========================================
// MINECORE 360 - DASHBOARD
// Electro Data S.A.C.
// Indicadores reales de MySQL
// ==========================================

document.addEventListener("DOMContentLoaded", function () {
  // ======================================
  // 1. MENÚ LATERAL RESPONSIVO
  // ======================================

  const menuToggle = document.getElementById("menuToggle");
  const sidebar = document.getElementById("sidebar");

  if (menuToggle && sidebar) {
    menuToggle.addEventListener("click", function () {
      const abierto = sidebar.classList.toggle("show");

      menuToggle.setAttribute("aria-expanded", String(abierto));
    });

    document.addEventListener("click", function (event) {
      if (window.innerWidth > 991) {
        return;
      }

      const clicDentro = sidebar.contains(event.target);
      const clicBoton = menuToggle.contains(event.target);

      if (!clicDentro && !clicBoton) {
        sidebar.classList.remove("show");

        menuToggle.setAttribute("aria-expanded", "false");
      }
    });
  }

  // ======================================
  // 2. GRÁFICO CON DATOS REALES
  // ======================================

  const canvas = document.getElementById("graficoSubsistemas");

  if (!canvas || typeof Chart === "undefined") {
    return;
  }

  // Leer valores enviados por Thymeleaf
  function obtenerNumero(nombre) {
    const valor = Number(canvas.dataset[nombre]);

    if (!Number.isFinite(valor) || valor < 0) {
      return 0;
    }

    return valor;
  }

  const datosSubsistemas = [
    obtenerNumero("cas"),
    obtenerNumero("oas"),
    obtenerNumero("mems"),
    obtenerNumero("rajant"),
    obtenerNumero("torsa"),
    obtenerNumero("gabinete"),
  ];

  // ======================================
  // 3. CONSTRUIR GRÁFICO
  // ======================================

  new Chart(canvas, {
    type: "bar",

    data: {
      labels: ["CAS", "OAS", "MEMS", "Rajant", "TORSA", "Gabinete"],

      datasets: [
        {
          label: "Evaluaciones registradas",

          data: datosSubsistemas,

          backgroundColor: [
            "#2563eb",
            "#fbbf24",
            "#16a34a",
            "#ef4444",
            "#8b5cf6",
            "#64748b",
          ],

          borderRadius: 7,

          maxBarThickness: 52,
        },
      ],
    },

    options: {
      responsive: true,

      maintainAspectRatio: false,

      plugins: {
        legend: {
          display: false,
        },

        tooltip: {
          enabled: true,
        },
      },

      scales: {
        y: {
          beginAtZero: true,

          ticks: {
            precision: 0,
            stepSize: 1,
          },
        },
      },
    },
  });
});
