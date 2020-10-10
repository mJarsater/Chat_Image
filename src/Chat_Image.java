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
    private int myPort, toPort;


    public Chat_Image(String[]args){
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


        myPort = Integer.parseInt(args[0]);
        toPort = Integer.parseInt(args[1]);
        new ImageReceiver(getImage, myPort);


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
            new ImageSender(image, toPort);


    }


}

class ImageSender {
    Image image;
    private int toPort;

    public ImageSender(Image image, int toPort){
        this.image = image;
        this.toPort = toPort;
        sendImage();
    }

    public void sendImage(){
        try {
            Socket socket = new Socket("localhost", toPort);
            OutputStream outputStream = socket.getOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);


            Graphics graphics = bufferedImage.createGraphics();
            graphics.drawImage(image,0,0,null);
            graphics.dispose();

            ImageIO.write(bufferedImage, "png", bufferedOutputStream);


            outputStream.close();
            bufferedOutputStream.close();
            socket.close();
            System.out.println(socket);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ImageReceiver extends Thread{
    private JLabel imageLabel;
    private int myPort;
    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream inputStream;
    private BufferedInputStream bufferedInputStream;

    public ImageReceiver(JLabel imageLabel, int myPort){
        this.imageLabel = imageLabel;
        this.myPort = myPort;
        start();
    }

    public void run() {
        try {
            while (true) {
                 serverSocket = new ServerSocket(myPort);
                 socket = serverSocket.accept();

                 inputStream = socket.getInputStream();
                 bufferedInputStream = new BufferedInputStream(inputStream);

                BufferedImage bufferedImage = ImageIO.read(bufferedInputStream);

                imageLabel.setIcon(new ImageIcon(bufferedImage));


                inputStream.close();
                bufferedInputStream.close();
                serverSocket.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void kill() throws IOException {
        inputStream.close();
        bufferedInputStream.close();
        serverSocket.close();
        socket.close();
    }


}
