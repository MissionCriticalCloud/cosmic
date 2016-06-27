//

//

package com.cloud.utils.crypt;

import com.cloud.utils.NumbersUtil;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class EncryptionSecretKeySender {
    public static void main(final String[] args) {
        try {
            // Create a socket to the host
            String hostname = "localhost";
            int port = 8097;

            if (args.length == 2) {
                hostname = args[0];
                port = NumbersUtil.parseInt(args[1], port);
            }
            final InetAddress addr = InetAddress.getByName(hostname);
            try (Socket socket = new Socket(addr, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                final java.io.BufferedReader stdin = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
                final String validationWord = "cloudnine";
                String validationInput = "";
                while (!validationWord.equals(validationInput)) {
                    System.out.print("Enter Validation Word:");
                    validationInput = stdin.readLine();
                    System.out.println();
                }
                System.out.print("Enter Secret Key:");
                final String input = stdin.readLine();
                if (input != null) {
                    out.println(input);
                }
            } catch (final Exception e) {
                System.out.println("Exception " + e.getMessage());
            }
        } catch (final Exception e) {
            System.out.print("Exception while sending secret key " + e);
        }
    }
}
