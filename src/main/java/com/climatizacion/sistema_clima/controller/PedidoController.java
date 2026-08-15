package com.climatizacion.sistema_clima.controller;

import com.climatizacion.sistema_clima.dto.PedidoDetalleResponseDTO;
import com.climatizacion.sistema_clima.dto.PedidoRequestDTO;
import com.climatizacion.sistema_clima.dto.PedidoResponseDTO;
import com.climatizacion.sistema_clima.entities.PedidoEntity;
import com.climatizacion.sistema_clima.entities.UsuarioEntity;
import com.climatizacion.sistema_clima.repository.UsuarioRepository;
import com.climatizacion.sistema_clima.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gestión de pedidos de equipos de aire acondicionado")
public class PedidoController {

    private final PedidoService service;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or (#dto.idUsuario == authentication.principal.idUsuario and hasAuthority('CLIENTE'))")
    @Operation(summary = "Crear un nuevo pedido", description = "Registra un pedido completo con sus productos. Solo ADMIN o el propio CLIENTE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente",
                    content = @Content(schema = @Schema(implementation = PedidoEntity.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    public ResponseEntity<?> crearPedidoCompleto(@RequestBody PedidoRequestDTO dto) {
        return new ResponseEntity<>(service.crearPedidoCompleto(dto), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Listar todos los pedidos", description = "Obtiene la lista completa de pedidos. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos")
    public List<PedidoEntity> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or @pedidoServiceImpl.obtenerPorId(#id).idUsuario == authentication.principal.idUsuario")
    @Operation(summary = "Obtener pedido por ID", description = "Devuelve los detalles completos de un pedido específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                    content = @Content(schema = @Schema(implementation = PedidoDetalleResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public PedidoDetalleResponseDTO obtener(
            @Parameter(description = "ID del pedido", required = true) @PathVariable Long id) {
        return service.obtenerPedidoConDetalles(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Eliminar pedido", description = "Elimina un pedido de la base de datos. Solo ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }

    @GetMapping("/usuario/{idUsuario}")
    @PreAuthorize("hasAuthority('ADMIN') or #idUsuario == authentication.principal.idUsuario")
    @Operation(summary = "Listar pedidos por usuario", description = "Obtiene todos los pedidos de un usuario específico.")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos del usuario")
    public List<PedidoEntity> listarPorUsuario(
            @Parameter(description = "ID del usuario", required = true) @PathVariable Long idUsuario) {
        return service.listarPorUsuario(idUsuario);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Cambiar estado del pedido", description = "Actualiza el estado de un pedido. Solo ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<Void> cambiarEstado(
            @Parameter(description = "ID del pedido", required = true) @PathVariable Long id,
            @Parameter(description = "Nuevo estado: Pendiente, En Proceso, Completado, Cancelado", required = true)
            @RequestParam String estado) {
        service.cambiarEstado(id, estado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Exportar pedidos a Excel", description = "Genera un archivo Excel con los pedidos filtrados. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Archivo Excel generado", content = @Content(mediaType = "application/octet-stream"))
    public ResponseEntity<byte[]> exportarPedidosAExcel(
            @Parameter(description = "Fecha de inicio (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha de fin (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) String estado,
            @Parameter(description = "ID del cliente") @RequestParam(required = false) Long idCliente,
            @Parameter(description = "Email del cliente") @RequestParam(required = false) String emailCliente) {

        if (emailCliente != null && !emailCliente.isEmpty() && idCliente == null) {
            UsuarioEntity usuario = usuarioRepository.findByEmail(emailCliente).orElse(null);
            if (usuario != null) {
                idCliente = usuario.getIdUsuario();
            }
        }

        List<PedidoEntity> pedidos = service.listarConFiltros(fechaInicio, fechaFin, estado, idCliente, emailCliente);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Workbook workbook = generarExcel(pedidos, fechaInicio, fechaFin, estado, idCliente, emailCliente);
            workbook.write(out);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String filename = "Reporte_Pedidos_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel: " + e.getMessage(), e);
        }
    }

    private Workbook generarExcel(List<PedidoEntity> pedidos, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                  String estado, Long idCliente, String emailCliente) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Reporte de Pedidos");

            // Estilos
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

            // Subtítulo
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
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el archivo Excel: " + e.getMessage(), e);
        }
    }

    @GetMapping("/conteos/pendientes/cliente/{idUsuario}")
    @PreAuthorize("hasAuthority('ADMIN') or #idUsuario == authentication.principal.idUsuario")
    @Operation(summary = "Contar pedidos pendientes de un cliente", description = "Devuelve la cantidad de pedidos en estado Pendiente o En Proceso de un cliente.")
    @ApiResponse(responseCode = "200", description = "Cantidad de pedidos pendientes")
    public ResponseEntity<Long> contarPedidosPendientesCliente(
            @Parameter(description = "ID del usuario") @PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.contarPedidosPorEstadoYUsuario(idUsuario, List.of("Pendiente", "En Proceso")));
    }

    @GetMapping("/conteos/pendientes/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Contar pedidos pendientes (admin)", description = "Devuelve la cantidad total de pedidos en estado Pendiente o En Proceso. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Cantidad de pedidos pendientes")
    public ResponseEntity<Long> contarPedidosPendientesAdmin() {
        return ResponseEntity.ok(service.contarPedidosPorEstado(List.of("Pendiente", "En Proceso")));
    }

    @GetMapping("/usuario/{idUsuario}/paginado")
    @PreAuthorize("hasAuthority('ADMIN') or #idUsuario == authentication.principal.idUsuario")
    @Operation(summary = "Listar pedidos de usuario con paginación", description = "Devuelve una página de pedidos de un usuario específico.")
    @ApiResponse(responseCode = "200", description = "Página de pedidos del usuario")
    public ResponseEntity<Page<PedidoResponseDTO>> listarPorUsuarioPaginado(
            @Parameter(description = "ID del usuario") @PathVariable Long idUsuario,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listarPorUsuarioPaginadoDTO(idUsuario, pageable));
    }

    @GetMapping("/paginado")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Listar pedidos paginados con filtros", description = "Devuelve una página de pedidos con filtros de búsqueda y estado. Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Página de pedidos")
    public ResponseEntity<Page<PedidoResponseDTO>> listarPedidosPaginados(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "8") int size,
            @Parameter(description = "Texto de búsqueda (ID o nombre de cliente)") @RequestParam(defaultValue = "") String search,
            @Parameter(description = "Filtrar por estado") @RequestParam(defaultValue = "") String estado) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPedido").descending());
        return ResponseEntity.ok(service.obtenerPedidosPaginados(search, estado, pageable));
    }
}