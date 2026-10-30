
package com.electrodata.minecore360.controller;

import com.electrodata.minecore360.entity.Inspeccion;
import com.electrodata.minecore360.repository.CamionRepository;
import com.electrodata.minecore360.repository.InspeccionRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class HomeController {

    private final CamionRepository camionRepository;
    private final InspeccionRepository inspeccionRepository;

    public HomeController(
            CamionRepository camionRepository,
            InspeccionRepository inspeccionRepository) {

        this.camionRepository = camionRepository;
        this.inspeccionRepository = inspeccionRepository;
    }

    // PAGINA PRINCIPAL
    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    // DASHBOARD CON DATOS REALES
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        long totalCamiones = camionRepository.count();

        List<Inspeccion> inspecciones =
                inspeccionRepository.findAll();

        long totalInspecciones = inspecciones.size();

        long pendientes = contarEstado(
                inspecciones, "PENDIENTE");

        long aprobadas = contarEstado(
                inspecciones, "APROBADA");

        long observadas = contarEstado(
                inspecciones, "OBSERVADA");

        long borradores = contarEstado(
                inspecciones, "BORRADOR");

        // Evaluaciones registradas por subsistema
        long totalCas = inspecciones.stream()
                .filter(i -> evaluado(i.getCas()))
                .count();

        long totalOas = inspecciones.stream()
                .filter(i -> evaluado(i.getOas()))
                .count();

        long totalMems = inspecciones.stream()
                .filter(i -> evaluado(i.getMems()))
                .count();

        long totalRajant = inspecciones.stream()
                .filter(i -> evaluado(i.getRajant()))
                .count();

        long totalTorsa = inspecciones.stream()
                .filter(i -> evaluado(i.getTorsa()))
                .count();

        long totalGabinete = inspecciones.stream()
                .filter(i -> evaluado(i.getGabinete()))
                .count();

        // Últimas cinco inspecciones
        List<Inspeccion> ultimasInspecciones =
                inspecciones.stream()
                        .sorted(
                            Comparator.comparing(
                                Inspeccion::getFechaInspeccion,
                                Comparator.nullsLast(
                                    Comparator.reverseOrder()
                                )
                            )
                        )
                        .limit(5)
                        .toList();

        // Indicadores principales
        model.addAttribute("totalCamiones", totalCamiones);
        model.addAttribute("totalInspecciones", totalInspecciones);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("aprobadas", aprobadas);
        model.addAttribute("observadas", observadas);
        model.addAttribute("borradores", borradores);

        // Datos reales para Chart.js
        model.addAttribute("totalCas", totalCas);
        model.addAttribute("totalOas", totalOas);
        model.addAttribute("totalMems", totalMems);
        model.addAttribute("totalRajant", totalRajant);
        model.addAttribute("totalTorsa", totalTorsa);
        model.addAttribute("totalGabinete", totalGabinete);

        model.addAttribute(
                "ultimasInspecciones",
                ultimasInspecciones
        );

        return "dashboard";
    }

    private long contarEstado(
            List<Inspeccion> inspecciones,
            String estado) {

        return inspecciones.stream()
                .filter(i -> estado.equals(i.getEstado()))
                .count();
    }

    private boolean evaluado(String valor) {
        return valor != null && !valor.isBlank();
    }
}
