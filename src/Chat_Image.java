import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class Chat_Image extends JFrame {
    private JLabel sendImage, getImage;
    private JButton chooseImgBtn, sendImageBtn;
    private File selectedImage;
    private JPanel sendImageArea, getImageArea;
    private ImageIcon imageIcon;
    private int port;
    private String address;
    private Socket socket;


    public Chat_Image(String[]args){
        try {
        sendImageArea = new JPanel();
        sendImageArea.setBounds(5,5,200,300);
        sendImageArea.setBackground(Color.gray);
        sendImage = new JLabel("No image chose");

        getImageArea = new JPanel();
        getImageArea.setBounds(225,5,200,300);
        getImageArea.setBackground(Color.gray);
        getImage = new JLabel("No image received");


        chooseImgBtn = new JButton("Choose image");
        chooseImgBtn.setBounds(5, 350, 200, 30);

        sendImageBtn = new JButton("Send image");
        sendImageBtn.setEnabled(false);
        sendImageBtn.setBounds(5, 390, 200, 30);

        sendImageArea.add(sendImage);
        getImageArea.add(getImage);
        this.add(chooseImgBtn);
        this.add(sendImageBtn);
        this.add(sendImageArea);
        this.add(getImageArea);
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
        new ImageReceiver(getImage, socket);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        new Chat_Image(args);
    }

    private void getImage(ActionEvent actionEvent) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            sendImageBtn.setEnabled(true);
            chooseImgBtn.setEnabled(false);
            selectedImage = fileChooser.getSelectedFile();
            try {
                imageIcon = new ImageIcon(ImageIO.read(selectedImage));
                sendImage.setIcon(imageIcon);
                sendImage.setText("");
            }catch (IOException ioe){
                System.out.println();
            }
        }
    }

    private void sendImage(ActionEvent actionEvent)  {
        System.out.println("Send clicked");
        Image image = imageIcon.getImage();
            new ImageSender(image, socket, sendImage);


    }


}

class ImageSender {
    private Image image;
    private Socket socket;
    private ObjectOutputStream in;
    private JLabel sendImage;

    public ImageSender(Image image, Socket socket, JLabel sendImage){
        this.image = image;
        this.socket = socket;
        this.sendImage = sendImage;
        sendImage();
    }

    public void sendImage(){
        try {
            in = new ObjectOutputStream(socket.getOutputStream());
            Icon icon = sendImage.getIcon();
            BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", bos);
            byte[]data = bos.toByteArray();
            String id = "message";

            Storage imageMsg = new Storage(data, id);


            in.writeObject(imageMsg);
            System.out.println("Image sent");

            in.reset();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ImageReceiver extends Thread{
    private JLabel imageLabel;
    private Socket socket;
    private ObjectInputStream in;

    public ImageReceiver(JLabel imageLabel, Socket socket){
        this.imageLabel = imageLabel;
        this.socket = socket;
        start();
    }

    public void run() {
        try {
            while (true) {
                in = new ObjectInputStream(socket.getInputStream());
                Storage storage = (Storage)in.readObject();

                ByteArrayInputStream inputStream = new ByteArrayInputStream(storage.getData());

                BufferedImage bufferedImage = ImageIO.read(inputStream);

                if(bufferedImage == null) {
                    imageLabel.setIcon(new ImageIcon(bufferedImage));
                    imageLabel.setText("Test");
                    System.out.println("Image received");
                } else{
                    imageLabel.setText("No image received");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void kill() throws IOException {
        socket.close();
    }


}
