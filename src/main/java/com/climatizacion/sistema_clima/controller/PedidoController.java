package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.PedidoRequestDTO;
import com.climatizacion.sistema_clima.dto.PedidoResponseDTO;
import com.climatizacion.sistema_clima.entities.PedidoEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;
    private final UsuarioRepository usuarioRepository;

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

    // ===== EXPORTAR EXCEL CON FILTROS =====
    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarPedidosAExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) String emailCliente) {

        // Si se proporciona email, buscar el ID del cliente
        if (emailCliente != null && !emailCliente.isEmpty() && idCliente == null) {
            UsuarioEntity usuario = usuarioRepository.findByEmail(emailCliente).orElse(null);
            if (usuario != null) {
                idCliente = usuario.getIdUsuario();
            }
        }

        try {
            List<PedidoEntity> pedidos = service.listarConFiltros(fechaInicio, fechaFin, estado, idCliente, emailCliente);
            Workbook workbook = generarExcel(pedidos, fechaInicio, fechaFin, estado, idCliente, emailCliente);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String filename = "Reporte_Pedidos_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Workbook generarExcel(List<PedidoEntity> pedidos, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                  String estado, Long idCliente, String emailCliente) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte de Pedidos");

        // Estilos (igual que antes)
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle subtitleStyle = workbook.createCellStyle();
        Font subtitleFont = workbook.createFont();
        subtitleFont.setItalic(true);
        subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        subtitleStyle.setFont(subtitleFont);
        subtitleStyle.setAlignment(HorizontalAlignment.RIGHT);

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

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataStyle);
        DataFormat format = workbook.createDataFormat();
        currencyStyle.setDataFormat(format.getFormat("$#,##0.00"));
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataStyle);
        dateStyle.setDataFormat(format.getFormat("dd/MM/yyyy HH:mm"));
        dateStyle.setAlignment(HorizontalAlignment.CENTER);

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

        // Título
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE PEDIDOS - SERVIA/C PRO");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        // Subtítulo con filtros
        Row subtitleRow = sheet.createRow(1);
        Cell subtitleCell = subtitleRow.createCell(0);
        StringBuilder filtros = new StringBuilder("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        if (fechaInicio != null) filtros.append(" | Desde: ").append(fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        if (fechaFin != null) filtros.append(" | Hasta: ").append(fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        if (estado != null && !estado.isEmpty()) filtros.append(" | Estado: ").append(estado);
        if (idCliente != null) filtros.append(" | Cliente ID: ").append(idCliente);
        if (emailCliente != null && !emailCliente.isEmpty()) filtros.append(" | Email: ").append(emailCliente);
        subtitleCell.setCellValue(filtros.toString());
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

        // Encabezados
        Row headerRow = sheet.createRow(3);
        String[] columnas = {"Factura ID", "Cliente", "Email", "Fecha", "Total", "Instalación", "Estado", "Dirección"};
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        sheet.createFreezePane(0, 4);

        double sumaTotal = 0.0;
        int rowNum = 4;
        for (PedidoEntity p : pedidos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getIdPedido());
            row.getCell(0).setCellStyle(dataStyle);

            // En el método generarExcel()
            // Buscar nombre y email del cliente
            String nombreCliente = "N/A";
            String emailClientePedido = "";
            Optional<UsuarioEntity> optUsuario = usuarioRepository.findById(p.getIdUsuario());
            if (optUsuario.isPresent()) {
                UsuarioEntity u = optUsuario.get();
                nombreCliente = u.getNombres() + " " + (u.getApellidos() != null ? u.getApellidos() : "");
                emailClientePedido = u.getEmail();
            }
            row.createCell(1).setCellValue(nombreCliente);
            row.getCell(1).setCellStyle(dataStyle);
            row.createCell(2).setCellValue(emailClientePedido);
            row.getCell(2).setCellStyle(dataStyle);

            Cell cellFecha = row.createCell(3);
            if (p.getFechaPedido() != null) {
                cellFecha.setCellValue(java.sql.Timestamp.valueOf(p.getFechaPedido()));
            }
            cellFecha.setCellStyle(dateStyle);

            double total = p.getTotal() != null ? p.getTotal() : 0.0;
            sumaTotal += total;
            Cell cellTotal = row.createCell(4);
            cellTotal.setCellValue(total);
            cellTotal.setCellStyle(currencyStyle);

            row.createCell(5).setCellValue(p.getIncluyeInstalacion() != null && p.getIncluyeInstalacion() ? "SÍ" : "NO");
            row.getCell(5).setCellStyle(dataStyle);

            row.createCell(6).setCellValue(p.getEstado() != null ? p.getEstado() : "");
            row.getCell(6).setCellStyle(dataStyle);

            row.createCell(7).setCellValue(p.getDireccion() != null ? p.getDireccion() : "");
            row.getCell(7).setCellStyle(dataStyle);
        }

        Row summaryRow = sheet.createRow(rowNum);
        Cell summaryLabelCell = summaryRow.createCell(0);
        summaryLabelCell.setCellValue("TOTAL RECAUDADO (" + pedidos.size() + " pedidos):");
        summaryLabelCell.setCellStyle(totalLabelStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 3));

        for (int i = 1; i <= 3; i++) {
            summaryRow.createCell(i).setCellStyle(totalLabelStyle);
        }

        Cell summaryValueCell = summaryRow.createCell(4);
        summaryValueCell.setCellValue(sumaTotal);
        summaryValueCell.setCellStyle(totalValueStyle);

        for (int i = 5; i < columnas.length; i++) {
            summaryRow.createCell(i).setCellStyle(totalLabelStyle);
        }

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(currentWidth + 1200, 20000));
        }

        return workbook;
    }

    @GetMapping("/conteos/pendientes/cliente/{idUsuario}")
    public ResponseEntity<Long> contarPedidosPendientesCliente(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.contarPedidosPorEstadoYUsuario(idUsuario, List.of("Pendiente", "En Proceso")));
    }

    @GetMapping("/conteos/pendientes/admin")
    public ResponseEntity<Long> contarPedidosPendientesAdmin() {
        return ResponseEntity.ok(service.contarPedidosPorEstado(List.of("Pendiente", "En Proceso")));
    }

    @GetMapping("/usuario/{idUsuario}/paginado")
    public ResponseEntity<Page<PedidoEntity>> listarPorUsuarioPaginado(
            @PathVariable Long idUsuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listarPorUsuarioPaginado(idUsuario, pageable));
    }

    @GetMapping("/paginado")
    public ResponseEntity<Page<PedidoResponseDTO>> listarPedidosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String estado) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPedido").descending());
        return ResponseEntity.ok(service.obtenerPedidosPaginados(search, estado, pageable));
    }
}