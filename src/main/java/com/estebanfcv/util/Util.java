/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.estebanfcv.util;

import com.sshtools.j2ssh.ScpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 *
 * @author estebanfcv
 */
public class Util {

    private static final int BUFFER_SIZE = 4096;

    public static boolean isCorreoValido(String correoElectronico) {
        boolean isValido = false;
        if (!"".equals(correoElectronico)) {
            try {
                new InternetAddress(correoElectronico, true).validate();
                isValido = true;
            } catch (AddressException ex) {
            }
        }
        return isValido;
    }

    public static String debugImprimirContenidoObjecto(Object o) {
        if (null == o) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("Objeto clase: ").append(o.getClass().getName()).append(" - ").append(o.toString()).append('\n');

        try {

            for (java.lang.reflect.Field f : o.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                sb = sb.append(f.getName()).append(" - ").append(f.get(o)).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String debugImprimirContenidoListaObjeto(Collection<? extends Object> lista) {
        if (null == lista) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("Collection clase: ").append(lista.getClass().getName()).append(" - ").append(lista.toString()).append('\n');

        try {

            for (Object o : lista) {

                sb = sb.append("Objeto clase: ").append(o.getClass().getName()).append(" - ").append(o.toString()).append('\n');

                for (java.lang.reflect.Field f : o.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    sb = sb.append(f.getName()).append(" - ").append(f.get(o)).append('\n');
                }

                sb = sb.append("=============================\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static boolean copiarArchivoServidorRemoto(File file, String nombreRemoto, int idEmp) {
        SshClient ssh = new SshClient();
        PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
        boolean operacionCorrecta = false;
        String ruta = "";
        try {
//            ssh.connect(c.getDireccionServidor(), new IgnoreHostKeyVerification());
//            pwd.setUsername(c.getUsernameServidor());
//            pwd.setPassword(c.getPassServidor());
            if (ssh.authenticate(pwd) == 4) {
                ScpClient scpClient = ssh.openScpClient();
                scpClient.put(new FileInputStream(file), file.length(), file.getName(), ruta);
                ssh.disconnect();
                operacionCorrecta = true;
            } else {
                throw new ConnectException("Error en la autenticacion de usuario");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                throw new ConnectException("No se pudo conectar al Servidor = ");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return operacionCorrecta;
    }

    public static boolean copiarArchivo(File fileOrigen, String ruta) {
        boolean exito = false;
        File fileDestino = new File(ruta);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(fileOrigen);
            out = new FileOutputStream(fileDestino);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            exito = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return exito;
    }

    public static boolean comprimirArchivo(File file, String nombreRemoto, int idEmp, String ruta) {
        boolean operacionCorrecta = false;
        try {
            compressFiles(file, new File(ruta + "/" + nombreRemoto + ".zip").toString());
            operacionCorrecta = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return operacionCorrecta;
    }

    private static void addFileToZip(File file, ZipOutputStream zos)
            throws FileNotFoundException, IOException {
        zos.setMethod(ZipOutputStream.DEFLATED);
        zos.setLevel(Deflater.BEST_COMPRESSION);
        zos.putNextEntry(new ZipEntry(file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
        }

        zos.closeEntry();
    }

    private static void compressFiles(File file, String destZipFile) throws FileNotFoundException, IOException {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(destZipFile));
            addFileToZip(file, zos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zos != null) {
                zos.flush();
                zos.close();
            }
        }
    }

    public static ZipInputStream getInputStream(File zip) throws IOException {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zip));
            zis.getNextEntry();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return zis;
    }

    public static void cerrarZip(List<ZipInputStream> lista) {
        try {
            for (ZipInputStream zis : lista) {
                if (zis != null) {
                    zis.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int compararObjetos(Object o1, Object o2) {
        if (null == o1 && null != o2) {
            return -1;
        } else if (null != o1 && null == o2) {
            return 1;
        } else if (null == o1 && null == o2) {
            return 0;
        } else {
            if (o1.getClass().isPrimitive() && o2.getClass().isPrimitive()) {
                return new Double((Double) o1).compareTo(new Double((Double) o2));
            }
            if (!o1.getClass().equals(o2.getClass())) {
                throw new IllegalArgumentException("The objects must be from the same instance");
            }
            if (o1 instanceof String && o2 instanceof String) {
                return ((String) o1).compareTo((String) o2);
            } else if (o1 instanceof Date && o2 instanceof Date) {
                return ((Date) o1).compareTo((Date) o2);
            } else if (o1 instanceof Integer && o2 instanceof Integer) {
                return ((Integer) o1).compareTo((Integer) o2);
            } else if (o1 instanceof Long && o2 instanceof Long) {
                return ((Long) o1).compareTo((Long) o2);
            } else if (o1 instanceof Float && o2 instanceof Float) {
                return ((Float) o1).compareTo((Float) o2);
            } else if (o1 instanceof Double && o2 instanceof Double) {
                return ((Double) o1).compareTo((Double) o2);
            } else if (o1 instanceof Boolean && o2 instanceof Boolean) {
                return ((Boolean) o1).compareTo((Boolean) o2);
            }
            return 0;
        }
    }

    public static int compararNumeros(double o1, double o2) {
        return new Double(o1).compareTo(new Double(o2));
    }

    public static boolean archivosPermitidos(String extension) {
        Map<String, String> mapaExtensionesPermitidas = new LinkedHashMap<>();
        mapaExtensionesPermitidas.put(".png", ".png");
        mapaExtensionesPermitidas.put(".jpg", ".jpg");
        mapaExtensionesPermitidas.put(".jpeg", ".jpeg");
        mapaExtensionesPermitidas.put(".gif", ".gif");
        return mapaExtensionesPermitidas.containsKey(extension);
    }

    public static byte[] encode(byte[] arr) {
        return Charset.forName("ISO-8859-15").encode(Charset.forName("UTF-8").decode(ByteBuffer.wrap(arr))).array();
    }

    public static void validarCondicionesPasswordNuevo(String passwordNuevo) {
        Pattern pattern = Pattern.compile("(?=^.{8,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*");
        Matcher matcher = pattern.matcher(passwordNuevo);
        try {
            if (8 > passwordNuevo.length()) {
//                mensaje.error(DiccionarioMensajes.ERROR_PASSWORD_MUY_CORTO);
            } else if (!matcher.matches()) {
//                mensaje.error(DiccionarioMensajes.ERROR_PASSWORD_NO_CUMPLE_CONDICIONES);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
