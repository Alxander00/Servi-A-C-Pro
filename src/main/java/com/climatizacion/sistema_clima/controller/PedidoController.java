package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.PedidoRequestDTO;
import com.climatizacion.sistema_clima.entities.PedidoEntity;
import com.climatizacion.sistema_clima.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService service;

    @PostMapping
    public ResponseEntity<?> crearPedidoCompleto(@RequestBody PedidoRequestDTO dto) {
        try {
            return new ResponseEntity<>(service.crearPedidoCompleto(dto), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public List<PedidoEntity> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PedidoEntity obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<PedidoEntity> listarPorUsuario(@PathVariable Long idUsuario) {
        return service.listarPorUsuario(idUsuario);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        service.cambiarEstado(id, estado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarPedidosAExcel() {
        try {
            List<PedidoEntity> pedidos = service.listar();
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Reporte de Ventas");

            // --- 1. CONFIGURACIÓN DE ESTILOS ---

            // Estilo Título
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Estilo Subtítulo (Fecha de generación)
            CellStyle subtitleStyle = workbook.createCellStyle();
            Font subtitleFont = workbook.createFont();
            subtitleFont.setItalic(true);
            subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            subtitleStyle.setFont(subtitleFont);
            subtitleStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Estilo Encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Estilo Datos Normales
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Estilo Moneda
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.cloneStyleFrom(dataStyle);
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("$#,##0.00"));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Estilo Fechas
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.cloneStyleFrom(dataStyle);
            dateStyle.setDataFormat(format.getFormat("dd/MM/yyyy HH:mm"));
            dateStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo Fila de Totales (Resaltado)
            CellStyle totalLabelStyle = workbook.createCellStyle();
            totalLabelStyle.cloneStyleFrom(dataStyle);
            totalLabelStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            totalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            totalLabelStyle.setFont(boldFont);

            CellStyle totalValueStyle = workbook.createCellStyle();
            totalValueStyle.cloneStyleFrom(currencyStyle);
            totalValueStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            totalValueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalValueStyle.setFont(boldFont);

            // --- 2. CONSTRUCCIÓN DEL REPORTE ---

            // Fila 0: Título Grande Combinado
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE OFICIAL DE VENTAS - CLIMAPRO");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Fila 1: Subtítulo con fecha
            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            subtitleCell.setCellValue("Generado el: " + fechaActual);
            subtitleCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

            // Fila 3: Encabezados de Tabla (Dejamos la fila 2 vacía para dar aire)
            Row headerRow = sheet.createRow(3);
            String[] columnas = {"Factura ID", "Cliente ID", "Fecha de Compra", "Total Pagado", "Instalación", "Estado", "Dirección"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Congelar el panel superior (Para que al hacer scroll, el título y encabezados no se pierdan)
            sheet.createFreezePane(0, 4);

            // Variables para sumar los totales
            double sumaTotalIngresos = 0.0;
            int totalOrdenes = pedidos.size();

            // Filas de Datos
            int rowNum = 4;
            for (PedidoEntity p : pedidos) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(p.getIdPedido());
                row.getCell(0).setCellStyle(dataStyle);

                row.createCell(1).setCellValue(p.getIdUsuario());
                row.getCell(1).setCellStyle(dataStyle);

                Cell cellFecha = row.createCell(2);
                if (p.getFechaPedido() != null) {
                    cellFecha.setCellValue(java.sql.Timestamp.valueOf(p.getFechaPedido()));
                }
                cellFecha.setCellStyle(dateStyle);

                double totalPedido = p.getTotal() != null ? p.getTotal() : 0.0;
                sumaTotalIngresos += totalPedido; // Acumulamos el total

                Cell cellTotal = row.createCell(3);
                cellTotal.setCellValue(totalPedido);
                cellTotal.setCellStyle(currencyStyle);

                Cell cellInstalacion = row.createCell(4);
                cellInstalacion.setCellValue(p.getIncluyeInstalacion() != null && p.getIncluyeInstalacion() ? "SÍ" : "NO");
                cellInstalacion.setCellStyle(dataStyle);

                Cell cellEstado = row.createCell(5);
                cellEstado.setCellValue(p.getEstado() != null ? p.getEstado() : "");
                cellEstado.setCellStyle(dataStyle);

                Cell cellDireccion = row.createCell(6);
                cellDireccion.setCellValue(p.getDireccion() != null ? p.getDireccion() : "");
                cellDireccion.setCellStyle(dataStyle);
            }

            // --- 3. FILA DE RESUMEN (TOTALES) ---
            Row summaryRow = sheet.createRow(rowNum);

            // Etiqueta del total (Combinamos de la columna 0 a la 2)
            Cell summaryLabelCell = summaryRow.createCell(0);
            summaryLabelCell.setCellValue("TOTAL RECAUDADO (" + totalOrdenes + " pedidos):");
            summaryLabelCell.setCellStyle(totalLabelStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 2));

            // Aplicar estilo a las celdas combinadas para que no se pierda el borde
            for (int i = 1; i <= 2; i++) {
                summaryRow.createCell(i).setCellStyle(totalLabelStyle);
            }

            // El valor sumado de todo el dinero
            Cell summaryValueCell = summaryRow.createCell(3);
            summaryValueCell.setCellValue(sumaTotalIngresos);
            summaryValueCell.setCellStyle(totalValueStyle);

            // Aplicar estilo vacío al resto de la fila para mantener la estética
            for (int i = 4; i < columnas.length; i++) {
                Cell emptyCell = summaryRow.createCell(i);
                emptyCell.setCellStyle(totalLabelStyle);
            }

            // --- 4. AUTO-AJUSTE DE COLUMNAS ---
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 1200); // Margen extra
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "Reporte_Ventas_ClimaPro.xlsx");

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/conteos/pendientes/cliente/{idUsuario}")
    public ResponseEntity<Long> contarPedidosPendientesCliente(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.contarPedidosPorEstadoYUsuario(idUsuario, List.of("Pendiente", "En Proceso")));
    }

    @GetMapping("/conteos/pendientes/admin")
    public ResponseEntity<Long> contarPedidosPendientesAdmin() {
        return ResponseEntity.ok(service.contarPedidosPorEstado(List.of("Pendiente", "En Proceso")));
    }
}