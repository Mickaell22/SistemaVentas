package Test;
import dao.impl.UsuarioDAOImpl;
import dao.interfaces.IUsuarioDAO;
import models.Usuario;
import models.Rol;
import java.util.List;
import java.util.Optional;

public class TestUsuarioDAO {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DEL DAO USUARIO ===\n");
        
        IUsuarioDAO usuarioDAO = new UsuarioDAOImpl();
        
        // Probar obtener todos los roles
        System.out.println("📋 Roles disponibles:");
        List<Rol> roles = usuarioDAO.obtenerTodosLosRoles();
        for (Rol rol : roles) {
            System.out.println("   " + rol.toString());
        }
        
        // Probar obtener todos los usuarios
        System.out.println("\n👥 Usuarios existentes:");
        List<Usuario> usuarios = usuarioDAO.obtenerTodos();
        for (Usuario usuario : usuarios) {
            System.out.println("   " + usuario.toString());
        }
        
        // Probar autenticación con el usuario admin por defecto
        System.out.println("\nPrueba de autenticación:");
        boolean authCorrect = usuarioDAO.autenticar("admin", "admin123");
        System.out.println("   Admin con contraseña correcta: " + authCorrect);
        
        boolean authIncorrect = usuarioDAO.autenticar("admin", "wrongpassword");
        System.out.println("   Admin con contraseña incorrecta: " + authIncorrect);
        
        // Probar crear nuevo usuario
        System.out.println("\nCreando nuevo usuario:");
        Usuario nuevoUsuario = new Usuario(
            "María", "García", "maria@email.com", "mgarcia",
            "Password123", "0987654321", "Av. Principal 123", 2
        );
        
        boolean usuarioCreado = usuarioDAO.crear(nuevoUsuario);
        System.out.println("   Usuario creado: " + usuarioCreado);
        
        if (usuarioCreado) {
            System.out.println("   ID asignado: " + nuevoUsuario.getId());
            
            // Probar autenticación del nuevo usuario
            boolean authNuevo = usuarioDAO.autenticar("mgarcia", "Password123");
            System.out.println("   Autenticación nuevo usuario: " + authNuevo);
        }
        
        // Probar obtener usuario por username
        System.out.println("\nBúsqueda por username:");
        Optional<Usuario> usuarioEncontrado = usuarioDAO.obtenerPorUsername("admin");
        if (usuarioEncontrado.isPresent()) {
            Usuario user = usuarioEncontrado.get();
            System.out.println("   Usuario encontrado: " + user.getNombreCompleto());
            System.out.println("   Rol: " + user.getRolNombre());
            System.out.println("   Email: " + user.getEmail());
        }
        
        // Probar validaciones
        System.out.println("\nValidaciones:");
        System.out.println("   Username 'admin' existe: " + usuarioDAO.existeUsername("admin"));
        System.out.println("   Username 'noexiste' existe: " + usuarioDAO.existeUsername("noexiste"));
        System.out.println("   Email 'admin@sistema.com' existe: " + usuarioDAO.existeEmail("admin@sistema.com"));
        
        // Probar obtener usuarios activos
        System.out.println("\nUsuarios activos:");
        List<Usuario> usuariosActivos = usuarioDAO.obtenerActivos();
        System.out.println("   Total usuarios activos: " + usuariosActivos.size());
        
        System.out.println("\nPruebas del DAO completadas!");
    }
}