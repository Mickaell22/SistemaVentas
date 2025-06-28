package models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Rol {
    private int id;
    private String nombre;
    private String descripcion;
    private int nivelJerarquia;
    private String permisosJson;
    private List<String> permisos;
    private boolean activo;
    private LocalDateTime fechaCreacion;

    // Constructores
    public Rol() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
        this.permisos = new ArrayList<>();
    }

    public Rol(String nombre, String descripcion, int nivelJerarquia) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivelJerarquia = nivelJerarquia;
    }

    // Métodos de utilidad para manejar permisos sin Gson
    public boolean hasPermission(String permiso) {
        if (permisos == null) {
            loadPermissionsFromJson();
        }
        return permisos != null && (permisos.contains(permiso) || permisos.contains("*"));
    }

    public boolean canDelegate(Rol otroRol) {
        return this.nivelJerarquia < otroRol.nivelJerarquia;
    }

    public List<String> getPermissionsList() {
        if (permisos == null) {
            loadPermissionsFromJson();
        }
        return permisos != null ? new ArrayList<>(permisos) : new ArrayList<>();
    }

    public boolean isValidHierarchy() {
        return nivelJerarquia >= 1 && nivelJerarquia <= 10;
    }

    /**
     * Carga permisos desde JSON usando parsing manual (sin Gson)
     */
    private void loadPermissionsFromJson() {
        if (permisosJson != null && !permisosJson.trim().isEmpty()) {
            try {
                // Parsing manual de JSON simple: ["permiso1", "permiso2"]
                String json = permisosJson.trim();
                
                if (json.startsWith("[") && json.endsWith("]")) {
                    // Remover corchetes
                    json = json.substring(1, json.length() - 1).trim();
                    
                    if (!json.isEmpty()) {
                        // Dividir por comas y limpiar comillas
                        String[] permisosArray = json.split(",");
                        permisos = new ArrayList<>();
                        
                        for (String permiso : permisosArray) {
                            // Remover comillas y espacios
                            permiso = permiso.trim();
                            if (permiso.startsWith("\"") && permiso.endsWith("\"")) {
                                permiso = permiso.substring(1, permiso.length() - 1);
                            }
                            if (!permiso.isEmpty()) {
                                permisos.add(permiso);
                            }
                        }
                    } else {
                        permisos = new ArrayList<>();
                    }
                } else {
                    // Si no es JSON válido, tratar como string simple
                    permisos = new ArrayList<>();
                    if (!json.isEmpty()) {
                        permisos.add(json);
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Error al cargar permisos desde JSON: " + e.getMessage());
                permisos = new ArrayList<>();
            }
        } else {
            permisos = new ArrayList<>();
        }
    }

    /**
     * Convierte lista de permisos a JSON usando formato manual (sin Gson)
     */
    public void setPermissionsList(List<String> permisos) {
        this.permisos = permisos != null ? new ArrayList<>(permisos) : new ArrayList<>();
        
        if (this.permisos.isEmpty()) {
            this.permisosJson = "[]";
        } else {
            // Crear JSON manual
            StringBuilder json = new StringBuilder();
            json.append("[");
            
            for (int i = 0; i < this.permisos.size(); i++) {
                if (i > 0) {
                    json.append(", ");
                }
                json.append("\"").append(this.permisos.get(i)).append("\"");
            }
            
            json.append("]");
            this.permisosJson = json.toString();
        }
    }

    /**
     * Agrega un permiso individual
     */
    public void addPermission(String permiso) {
        if (permisos == null) {
            loadPermissionsFromJson();
        }
        if (!permisos.contains(permiso)) {
            permisos.add(permiso);
            setPermissionsList(permisos); // Actualizar JSON
        }
    }

    /**
     * Remueve un permiso individual
     */
    public void removePermission(String permiso) {
        if (permisos == null) {
            loadPermissionsFromJson();
        }
        if (permisos.remove(permiso)) {
            setPermissionsList(permisos); // Actualizar JSON
        }
    }

    /**
     * Establece permisos desde array
     */
    public void setPermissions(String... permisos) {
        setPermissionsList(Arrays.asList(permisos));
    }

    public String getDisplayText() {
        return String.format("%s (Nivel %d)", nombre, nivelJerarquia);
    }

    public String getPermisosTexto() {
        List<String> listaPermisos = getPermissionsList();
        if (listaPermisos.isEmpty()) {
            return "Sin permisos";
        }
        return String.join(", ", listaPermisos);
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getNivelJerarquia() { return nivelJerarquia; }
    public void setNivelJerarquia(int nivelJerarquia) { this.nivelJerarquia = nivelJerarquia; }

    public String getPermisosJson() { return permisosJson; }
    public void setPermisosJson(String permisosJson) { 
        this.permisosJson = permisosJson;
        this.permisos = null; // Reset para recargar
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() {
        return getDisplayText();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rol rol = (Rol) obj;
        return id == rol.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}