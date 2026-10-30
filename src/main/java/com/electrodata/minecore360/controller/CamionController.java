
package com.electrodata.minecore360.controller;

import com.electrodata.minecore360.entity.Camion;
import com.electrodata.minecore360.service.CamionService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/camiones")
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    // LISTAR CAMIONES
    @GetMapping
    public String listar(Model model) {

        model.addAttribute(
            "camiones",
            camionService.listarTodos()
        );

        return "camiones/lista";
    }

    // MOSTRAR FORMULARIO NUEVO
    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute("camion", new Camion());

        return "camiones/formulario";
    }

    // GUARDAR CAMION
    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("camion") Camion camion,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "camiones/formulario";
        }

        try {

            camionService.guardar(camion);

            redirectAttributes.addFlashAttribute(
                "mensaje",
                "Camión guardado correctamente"
            );

            return "redirect:/camiones";

        } catch (IllegalArgumentException e) {

            result.rejectValue(
                "codigo",
                "codigo.duplicado",
                e.getMessage()
            );

            return "camiones/formulario";
        }
    }

    // EDITAR CAMION
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        return camionService.buscarPorId(id)
            .map(camion -> {

                model.addAttribute("camion", camion);

                return "camiones/formulario";
            })
            .orElseGet(() -> {

                redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró el camión solicitado"
                );

                return "redirect:/camiones";
            });
    }
}
