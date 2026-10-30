
package com.electrodata.minecore360.controller;

import com.electrodata.minecore360.entity.Camion;
import com.electrodata.minecore360.entity.Inspeccion;

import com.electrodata.minecore360.repository.CamionRepository;

import com.electrodata.minecore360.service.CamionService;
import com.electrodata.minecore360.service.InspeccionService;
import com.electrodata.minecore360.service.InspeccionExcelService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/inspecciones")
public class InspeccionController {

    private final InspeccionService inspeccionService;
    private final CamionService camionService;
    private final CamionRepository camionRepository;
    private final InspeccionExcelService inspeccionExcelService;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================
    public InspeccionController(
            InspeccionService inspeccionService,
            CamionService camionService,
            CamionRepository camionRepository,
            InspeccionExcelService inspeccionExcelService) {

        this.inspeccionService = inspeccionService;
        this.camionService = camionService;
        this.camionRepository = camionRepository;
        this.inspeccionExcelService = inspeccionExcelService;
    }

    // ==========================================
    // VALIDAR ROL
    // ==========================================
    private boolean tieneRol(
            Authentication auth,
            String rol) {

        return auth.getAuthorities().stream()
                .anyMatch(a ->
                    a.getAuthority().equals("ROLE_" + rol));
    }

    private boolean esEstandar(Authentication auth) {
        return tieneRol(auth, "USUARIO");
    }

    private boolean esAdmin(Authentication auth) {
        return tieneRol(auth, "ADMIN");
    }

    private boolean puedeSupervisar(Authentication auth) {
        return tieneRol(auth, "SUPERVISOR")
                || tieneRol(auth, "ADMIN");
    }

    // ==========================================
    // DATOS DEL USUARIO PARA LA VISTA
    // ==========================================
    private void agregarDatosSeguridad(
            Model model,
            Authentication auth) {

        model.addAttribute(
                "esEstandar", esEstandar(auth));

        model.addAttribute(
                "esAdmin", esAdmin(auth));

        model.addAttribute(
                "esSupervisor", puedeSupervisar(auth));

        model.addAttribute(
                "usuarioActual", auth.getName());
    }

    // ==========================================
    // BUSCAR INSPECCION POR ID
    // ==========================================
    private Inspeccion obtenerInspeccion(Long id) {

        return inspeccionService.buscarPorId(id)
                .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Inspección no encontrada"));
    }

    // ==========================================
    // VERIFICAR PROPIETARIO
    // ==========================================
    private void verificarAcceso(
            Inspeccion inspeccion,
            Authentication auth) {

        if (esEstandar(auth)
                && !Objects.equals(
                        inspeccion.getUsuarioRegistro(),
                        auth.getName())) {

            throw new AccessDeniedException(
                    "No tiene permiso para acceder "
                    + "a esta inspección");
        }
    }

    // ==========================================
    // BUSQUEDA CON FILTROS Y PERMISOS
    // ==========================================
    private List<Inspeccion> obtenerResultados(
            Long camionId,
            String tecnico,
            LocalDate fecha,
            String estado,
            Authentication auth) {

        List<Inspeccion> resultados =
                inspeccionService.buscarConFiltros(
                        camionId,
                        tecnico,
                        fecha,
                        estado);

        // USUARIO ESTANDAR: SOLO SUS REGISTROS
        if (esEstandar(auth)) {

            return resultados.stream()
                    .filter(i -> Objects.equals(
                            i.getUsuarioRegistro(),
                            auth.getName()))
                    .toList();
        }

        // SUPERVISOR / ADMINISTRADOR:
        // TODOS LOS REGISTROS
        return resultados;
    }

    // ==========================================
    // 1. HISTORIAL DE INSPECCIONES
    // ==========================================
    @GetMapping
    public String listar(
            @RequestParam(required = false)
            Long camionId,

            @RequestParam(required = false)
            String tecnico,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,

            @RequestParam(required = false)
            String estado,

            Authentication auth,
            Model model) {

        List<Inspeccion> inspecciones =
                obtenerResultados(
                        camionId,
                        tecnico,
                        fecha,
                        estado,
                        auth);

        model.addAttribute(
                "inspecciones",
                inspecciones);

        model.addAttribute(
                "camiones",
                camionService.listarTodos());

        model.addAttribute("camionId", camionId);
        model.addAttribute("tecnico", tecnico);
        model.addAttribute("fecha", fecha);
        model.addAttribute("estado", estado);

        agregarDatosSeguridad(model, auth);

        return "inspecciones/lista";
    }

    // ==========================================
    // 2. NUEVA INSPECCION
    // ==========================================
    @GetMapping("/nueva")
    public String nueva(
            Authentication auth,
            Model model) {

        Inspeccion inspeccion = new Inspeccion();

        inspeccion.setEstado("BORRADOR");

        model.addAttribute(
                "inspeccion",
                inspeccion);

        model.addAttribute(
                "camiones",
                camionService.listarTodos());

        agregarDatosSeguridad(model, auth);

        return "inspecciones/formulario";
    }

    // ==========================================
    // 3. EDITAR INSPECCION
    // ==========================================
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Authentication auth,
            Model model) {

        Inspeccion inspeccion =
                obtenerInspeccion(id);

        verificarAcceso(inspeccion, auth);

        model.addAttribute(
                "inspeccion",
                inspeccion);

        model.addAttribute(
                "camiones",
                camionService.listarTodos());

        agregarDatosSeguridad(model, auth);

        return "inspecciones/formulario";
    }

    // ==========================================
    // 4. GUARDAR O ACTUALIZAR INSPECCION
    // ==========================================
    @PostMapping("/guardar")
    public String guardar(
            @Valid
            @ModelAttribute("inspeccion")
            Inspeccion formulario,

            BindingResult resultado,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes) {

        boolean esEdicion = formulario.getId() != null;

        Inspeccion inspeccion;

        if (esEdicion) {

            // Cargar entidad original desde MySQL
            inspeccion =
                    obtenerInspeccion(formulario.getId());

            verificarAcceso(inspeccion, auth);

        } else {

            inspeccion = new Inspeccion();

            // El propietario nunca se toma del HTML
            inspeccion.setUsuarioRegistro(
                    auth.getName());
        }

        // ======================================
        // VALIDAR CAMION
        // ======================================
        Long camionId = null;

        if (formulario.getCamion() != null) {
            camionId = formulario.getCamion().getId();
        }

        Camion camion = null;

        if (camionId == null) {

            if (!resultado.hasFieldErrors("camion")) {
                resultado.rejectValue(
                        "camion",
                        "camion.obligatorio",
                        "Debe seleccionar un camión");
            }

        } else {

            camion = camionRepository
                    .findById(camionId)
                    .orElse(null);

            if (camion == null) {

                resultado.rejectValue(
                        "camion",
                        "camion.invalido",
                        "El camión seleccionado no existe");
            }
        }

        // ======================================
        // VALIDAR SUBSISTEMAS
        // ======================================
        validarSubsistema(
                resultado,
                "cas",
                formulario.getCas());

        validarSubsistema(
                resultado,
                "oas",
                formulario.getOas());

        validarSubsistema(
                resultado,
                "mems",
                formulario.getMems());

        validarSubsistema(
                resultado,
                "rajant",
                formulario.getRajant());

        validarSubsistema(
                resultado,
                "torsa",
                formulario.getTorsa());

        validarSubsistema(
                resultado,
                "gabinete",
                formulario.getGabinete());

        // ======================================
        // CONTROL DE ESTADOS POR ROL
        // ======================================
        if (esEstandar(auth)) {

            String nuevoEstado = formulario.getEstado();

            // El estándar solo puede colocar
            // BORRADOR o PENDIENTE
            if (!"BORRADOR".equals(nuevoEstado)
                    && !"PENDIENTE".equals(nuevoEstado)) {

                resultado.rejectValue(
                        "estado",
                        "estado.sinPermiso",
                        "Solo puede utilizar los estados "
                        + "BORRADOR o PENDIENTE");
            }

            // No permitir que un usuario estándar
            // revierta una inspección ya revisada.
            if (esEdicion
                    && ("APROBADA".equals(inspeccion.getEstado())
                    || "OBSERVADA".equals(inspeccion.getEstado()))) {

                throw new AccessDeniedException(
                        "Una inspección revisada "
                        + "no puede ser modificada "
                        + "por el usuario estándar");
            }
        }

        // ======================================
        // MOSTRAR VALIDACIONES
        // ======================================
        if (resultado.hasErrors()) {

            model.addAttribute(
                    "camiones",
                    camionService.listarTodos());

            model.addAttribute(
                    "mensajeError",
                    "Revise los campos del formulario.");

            agregarDatosSeguridad(model, auth);

            return "inspecciones/formulario";
        }

        // ======================================
        // COPIAR CAMPOS PERMITIDOS
        // ======================================
        // No copiamos id ni usuarioRegistro
        // desde la solicitud HTTP.

        inspeccion.setCamion(camion);
        inspeccion.setFechaInspeccion(
                formulario.getFechaInspeccion());

        inspeccion.setHorometro(
                formulario.getHorometro());

        inspeccion.setTecnico(
                formulario.getTecnico());

        inspeccion.setCas(
                formulario.getCas());

        inspeccion.setOas(
                formulario.getOas());

        inspeccion.setMems(
                formulario.getMems());

        inspeccion.setRajant(
                formulario.getRajant());

        inspeccion.setTorsa(
                formulario.getTorsa());

        inspeccion.setGabinete(
                formulario.getGabinete());

        inspeccion.setObservaciones(
                formulario.getObservaciones());

        inspeccion.setEstado(
                formulario.getEstado());

        // ======================================
        // GUARDAR EN MYSQL
        // ======================================
        inspeccionService.guardar(inspeccion);

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                esEdicion
                    ? "La inspección se actualizó correctamente."
                    : "La inspección se registró correctamente.");

        return "redirect:/inspecciones";
    }

    // ==========================================
    // VALIDAR VALORES DE SUBSISTEMAS
    // ==========================================
    private void validarSubsistema(
            BindingResult resultado,
            String campo,
            String valor) {

        if (!"OPERATIVO".equals(valor)
                && !"OBSERVADO".equals(valor)
                && !"NO OPERATIVO".equals(valor)) {

            resultado.rejectValue(
                    campo,
                    "subsistema.invalido",
                    "Seleccione un estado válido");
        }
    }

    // ==========================================
    // 5. DETALLE DE INSPECCION
    // ==========================================
    @GetMapping("/detalle/{id}")
    public String detalle(
            @PathVariable Long id,
            Authentication auth,
            Model model) {

        Inspeccion inspeccion =
                obtenerInspeccion(id);

        verificarAcceso(inspeccion, auth);

        model.addAttribute(
                "inspeccion",
                inspeccion);

        agregarDatosSeguridad(model, auth);

        return "inspecciones/detalle";
    }

    // ==========================================
    // 6. ELIMINAR - SOLO ADMINISTRADOR
    // ==========================================
    @PostMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Long id,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(auth)) {

            throw new AccessDeniedException(
                    "Solo el administrador "
                    + "puede eliminar inspecciones");
        }

        obtenerInspeccion(id);

        inspeccionService.eliminar(id);

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "La inspección se eliminó correctamente.");

        return "redirect:/inspecciones";
    }

    // ==========================================
    // 7. EXPORTAR EXCEL
    // ==========================================
    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false)
            Long camionId,

            @RequestParam(required = false)
            String tecnico,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,

            @RequestParam(required = false)
            String estado,

            Authentication auth
    ) throws IOException {

        // Se aplican los mismos filtros
        // que aparecen en el historial.
        //
        // USUARIO: solo registros propios.
        // SUPERVISOR: todos los registros.
        // ADMIN: todos los registros.

        List<Inspeccion> inspecciones =
                obtenerResultados(
                        camionId,
                        tecnico,
                        fecha,
                        estado,
                        auth);

        byte[] archivo =
                inspeccionExcelService.generarExcel(
                        inspecciones);

        String nombreArchivo =
                "MINECORE360_Inspecciones_"
                + LocalDate.now()
                + ".xlsx";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                        + nombreArchivo
                        + "\"")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(archivo.length)
                .body(archivo);
    }
}
