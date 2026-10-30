
package com.electrodata.minecore360.service;

import com.electrodata.minecore360.entity.Inspeccion;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InspeccionExcelService {

    private static final String[] COLUMNAS = {
        "ID",
        "CAMIÓN",
        "FECHA Y HORA",
        "TÉCNICO",
        "HORÓMETRO",
        "CAS",
        "OAS",
        "MEMS",
        "RAJANT",
        "TORSA",
        "GABINETE",
        "ESTADO GENERAL",
        "OBSERVACIONES"
    };

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarExcel(
            List<Inspeccion> inspecciones) throws IOException {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            Sheet hoja = workbook.createSheet("Inspecciones");

            // TITULO
            Font fuenteTitulo = workbook.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 17);
            fuenteTitulo.setColor(IndexedColors.WHITE.getIndex());

            CellStyle estiloTitulo = workbook.createCellStyle();
            estiloTitulo.setFillForegroundColor(
                    IndexedColors.DARK_BLUE.getIndex());
            estiloTitulo.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND);
            estiloTitulo.setFont(fuenteTitulo);
            estiloTitulo.setVerticalAlignment(
                    VerticalAlignment.CENTER);

            Row filaTitulo = hoja.createRow(0);
            filaTitulo.setHeightInPoints(35);

            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue(
                    "MINECORE 360 | REPORTE DE INSPECCIONES");
            celdaTitulo.setCellStyle(estiloTitulo);

            hoja.addMergedRegion(
                    new CellRangeAddress(0, 0, 0, 12));

            // SUBTITULO
            Row filaSubtitulo = hoja.createRow(1);

            filaSubtitulo.createCell(0).setCellValue(
                    "Electro Data S.A.C. - Mantenimiento "
                    + "preventivo tecnológico minero");

            hoja.addMergedRegion(
                    new CellRangeAddress(1, 1, 0, 12));

            // ENCABEZADOS
            Font fuenteEncabezado = workbook.createFont();
            fuenteEncabezado.setBold(true);
            fuenteEncabezado.setColor(
                    IndexedColors.BLACK.getIndex());

            CellStyle estiloEncabezado =
                    workbook.createCellStyle();

            estiloEncabezado.setFillForegroundColor(
                    IndexedColors.GOLD.getIndex());
            estiloEncabezado.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND);
            estiloEncabezado.setFont(fuenteEncabezado);
            estiloEncabezado.setWrapText(true);

            Row encabezado = hoja.createRow(3);
            encabezado.setHeightInPoints(30);

            for (int i = 0; i < COLUMNAS.length; i++) {
                Cell celda = encabezado.createCell(i);
                celda.setCellValue(COLUMNAS[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            // ESTILO DE CELDAS
            CellStyle estiloNormal = workbook.createCellStyle();
            estiloNormal.setVerticalAlignment(
                    VerticalAlignment.CENTER);

            CellStyle estiloObservaciones =
                    workbook.createCellStyle();
            estiloObservaciones.setWrapText(true);
            estiloObservaciones.setVerticalAlignment(
                    VerticalAlignment.TOP);

            // DATOS
            int numeroFila = 4;

            for (Inspeccion inspeccion : inspecciones) {

                Row fila = hoja.createRow(numeroFila++);
                fila.setHeightInPoints(25);

                fila.createCell(0).setCellValue(
                        inspeccion.getId());

                fila.createCell(1).setCellValue(
                        inspeccion.getCamion() != null
                                ? inspeccion.getCamion().getCodigo()
                                : "");

                fila.createCell(2).setCellValue(
                        inspeccion.getFechaInspeccion() != null
                                ? inspeccion.getFechaInspeccion()
                                    .format(FORMATO_FECHA)
                                : "");

                fila.createCell(3).setCellValue(
                        texto(inspeccion.getTecnico()));

                Cell horometro = fila.createCell(4);

                if (inspeccion.getHorometro() != null) {
                    horometro.setCellValue(
                            inspeccion.getHorometro().doubleValue());
                }

                fila.createCell(5).setCellValue(
                        texto(inspeccion.getCas()));

                fila.createCell(6).setCellValue(
                        texto(inspeccion.getOas()));

                fila.createCell(7).setCellValue(
                        texto(inspeccion.getMems()));

                fila.createCell(8).setCellValue(
                        texto(inspeccion.getRajant()));

                fila.createCell(9).setCellValue(
                        texto(inspeccion.getTorsa()));

                fila.createCell(10).setCellValue(
                        texto(inspeccion.getGabinete()));

                fila.createCell(11).setCellValue(
                        texto(inspeccion.getEstado()));

                Cell observaciones = fila.createCell(12);
                observaciones.setCellValue(
                        texto(inspeccion.getObservaciones()));
                observaciones.setCellStyle(estiloObservaciones);
            }

            // ANCHOS DE COLUMNAS
            int[] anchos = {
                10, 20, 23, 28, 16,
                18, 18, 18, 18, 18,
                18, 20, 55
            };

            for (int i = 0; i < anchos.length; i++) {
                hoja.setColumnWidth(i, anchos[i] * 256);
            }

            // FILTROS DE EXCEL
            hoja.setAutoFilter(
                    new CellRangeAddress(
                            3,
                            Math.max(3, numeroFila - 1),
                            0,
                            12
                    )
            );

            // CONGELAR ENCABEZADOS
            hoja.createFreezePane(0, 4);

            workbook.write(salida);

            return salida.toByteArray();
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }
}
