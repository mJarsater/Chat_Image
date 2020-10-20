import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class Chat_Image extends JFrame {
    private JLabel sendImage, getImage, sendMesssageLabel;
    private JTextField sendMessageField;
    private JTextArea getMessageTextArea;
    private JButton chooseImgBtn, sendImageBtn;
    private File selectedImage;
    private ImageIcon imageIcon;
    private int port;
    private String address, localPath;
    private Socket socket;

    /* Konstruktor för klassen Chat_Image - GUI*/
    public Chat_Image(){
        try {
        sendImage = new JLabel();
        sendImage.setBounds(5,5, 200, 300);
        sendMesssageLabel = new JLabel("Message: ");
        sendMesssageLabel.setBounds(5, 310, 60, 20);
        sendMessageField = new JTextField();
        sendMessageField.setBounds(65, 310, 135, 20);



        getImage = new JLabel();
        getImage.setBounds(225,5,200,300);
        getMessageTextArea = new JTextArea();
        getMessageTextArea.setEditable(false);
        getMessageTextArea.setBounds(225, 340, 200, 100);
        chooseImgBtn = new JButton("Choose image");
        chooseImgBtn.setBounds(5, 350, 200, 30);

        sendImageBtn = new JButton("Send image");
        sendImageBtn.setEnabled(false);
        sendImageBtn.setBounds(5, 390, 200, 30);



        this.add(chooseImgBtn);
        this.add(sendImageBtn);
        this.add(sendMesssageLabel);
        this.add(sendMessageField);
        this.add(getMessageTextArea);
        this.add(sendImage);
        this.add(getImage);
        sendImageBtn.addActionListener(this:: sendImage);
        chooseImgBtn.addActionListener(this:: getImage);


        this.setLayout(null);
        this.setTitle("Imagetransfer");
        this.setSize(500,500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);


        port = 4848;
        address = "atlas.dsv.su.se";
        socket = new Socket(address, port);
        setTitle("Connected...");

        new ImageReceiver(getImage, socket, getMessageTextArea);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        new Chat_Image();
    }
    /* Metod som hämtar en bild via FileChooser,
    * sätter denna bild till "selectedImage" som
    * visas upp i sendLabel.
    *
    * Samt skalar om bilden så att den passar. */
    private void getImage(ActionEvent actionEvent) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            sendImageBtn.setEnabled(true);
            chooseImgBtn.setEnabled(false);
            selectedImage = fileChooser.getSelectedFile();
            try {
                imageIcon = new ImageIcon(ImageIO.read(selectedImage));
                Image scaledImage = imageIcon.getImage().getScaledInstance(sendImage.getWidth(), sendImage.getHeight(), Image.SCALE_SMOOTH);
                localPath = selectedImage.getPath();
                sendImage.setIcon(new ImageIcon(scaledImage));
                sendImage.setText("");
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
    }
    /* Metod som tar den valda bilder, samt
    * eventuellt meddelande och skapar ett
    * nytt imagesender-objekt. */
    private void sendImage(ActionEvent actionEvent)  {
        if(sendMessageField.getText().equals("")){
            String msg = "No message";
            new ImageSender( socket,  localPath ,msg);
            sendImageBtn.setEnabled(false);
            chooseImgBtn.setEnabled(true);
        } else {
            String msg = sendMessageField.getText();
            new ImageSender( socket,  localPath ,msg);
            sendImageBtn.setEnabled(false);
            chooseImgBtn.setEnabled(true);
    }




}

class ImageSender {

    private Socket socket;
    private ObjectOutputStream out;
    private String msg;
    private String localPath;

    // Konstruktor för klassen ImageSender
    public ImageSender(Socket socket, String localPath, String msg){
        this.msg = msg;
        this.socket = socket;
        this.localPath = localPath;
        sendImage();
    }

    /* Metod som skapar en objectoutputstream mot socketens
    * outputstream.
    *
    * Skriver den buffrade bilden till en byteArrayOutputStream,
    * samt "filtyp" via ImageIO.
    *
    * Skapar ett nytt Storage objekt med byteArrayOutputStreamen
    * och eventuellt meddelande.
    *
    * Skriver sen objektet till socketens ObjectOutputStream*/
    public void sendImage(){
        try {
            out = new ObjectOutputStream(socket.getOutputStream());

            BufferedImage bufferedImage = getBufferedImage(localPath);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);

            Storage message = new Storage(byteArrayOutputStream, msg);

            out.writeObject(message);

            out.reset();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /* Metod som skapar och returnerar en BufferedImage
    av filen som finns på den sökväg som angets. */
    public BufferedImage getBufferedImage(String filepath) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(filepath));
            return bufferedImage;
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        return null;
    }
}

class ImageReceiver extends Thread {
    private JLabel imageLabel;
    private JTextArea getMessageTextArea;
    private Socket socket;
    private ObjectInputStream in;


    // Konstruktor för klassen ImageReceiver
    public ImageReceiver(JLabel imageLabel, Socket socket, JTextArea getMessageTextArea) {
        this.getMessageTextArea = getMessageTextArea;
        this.imageLabel = imageLabel;
        this.socket = socket;
        start();
    }
    /* Metod som skapar en ObjectInputStream från socketen.
    *  Skapar Storage objekt av de objekt som kommer in
    *  från streamen.
    *
    *  Kallar sen på: getBufferedImage & getImageIcon,
    *  och gör om de som har returnerats till en Image,
    *  som kan skalas om för att passa imageLabeln.
    *  */
    public void run() {
        try {
            while (true) {
                in = new ObjectInputStream(socket.getInputStream());
                Storage storage = (Storage) in.readObject();

                byte[] data = storage.getData();
                String msg = storage.getId();

                BufferedImage bufferedImage = getBufferedImage(data);

                ImageIcon receivedImage = getImageIcon(bufferedImage);

                Image scaledImage = receivedImage.getImage().getScaledInstance(getImage.getWidth(), getImage.getHeight(), Image.SCALE_SMOOTH);

                imageLabel.setIcon(new ImageIcon(scaledImage));
                getMessageTextArea.setText(msg);

            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /* Metod som skapar en ByteArrayInputStream men arrayen "data",
    *  samt skapar en BufferedImage av den streamen.
    *  Returnerar en BufferedImage  */
    public BufferedImage getBufferedImage(byte[]data){
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            BufferedImage receivedBufferedImage = ImageIO.read(byteArrayInputStream);
            return receivedBufferedImage;
        }catch (IOException io){
            io.printStackTrace();
        }
        return null;
    }
    /* Metod som skapar en ImageIcon av parametern
    *  bufferedImage, returnerar sen en ImageIcon */
    public ImageIcon getImageIcon(BufferedImage bufferedImage){
        ImageIcon receivedImage = new ImageIcon(bufferedImage);
        return receivedImage;
    }
}
}
