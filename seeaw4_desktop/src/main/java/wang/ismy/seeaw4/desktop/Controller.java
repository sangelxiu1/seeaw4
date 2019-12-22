package wang.ismy.seeaw4.desktop;

import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.WindowEvent;
import wang.ismy.seeaw4.client.Client;
import wang.ismy.seeaw4.client.client.LocalPer;
import wang.ismy.seeaw4.client.terminal.TerminalProxy;
import wang.ismy.seeaw4.common.ExecuteService;
import wang.ismy.seeaw4.common.client.Per;
import wang.ismy.seeaw4.terminal.camera.Camera;
import wang.ismy.seeaw4.terminal.desktop.Desktop;
import wang.ismy.seeaw4.terminal.observer.impl.LazyTerminalObserver;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


/**
 * @author MY
 * @date 2019/12/21 14:27
 */
public class Controller {

    @FXML
    private JFXListView<ClientView> listView;

    @FXML
    private AnchorPane ap;

    private Client client;
    private ExecuteService executeService = ExecuteService.getInstance();

    public Controller() {
        client = new Client();
        client.getTerminalProxy().registerObserver(new LazyTerminalObserver() {
            @Override
            public void onMessage(String msg) {
                System.err.print(msg);
            }
        });
        client.setClientListChangeListener(this::renderClientList);
        client.init();
    }

    private void renderClientList(List<Per> list) {
        System.out.println("client list render");
        Platform.runLater(() -> {
            listView.getItems().clear();
            List<LocalPer> localPerList = client.getLocalPerList();
            for (LocalPer per : localPerList) {
                ClientView clientView = new ClientView();
                clientView.setClient(per);
                // 显示画面
                executeService.excute(()->{
                    TerminalProxy terminalProxy = per.getTerminalProxy();
                    if (terminalProxy != null) {
                        Desktop desktop = terminalProxy.getDesktop();
                        Camera camera = terminalProxy.getCamera();
                        byte[] screenBytes = desktop.getScreen(null, null);
                        byte[] cameraBytes = camera.getCameraSnapshot(null, null);
                        Platform.runLater(()->{
                            clientView.setScreen(new Image(new ByteArrayInputStream(screenBytes)));
                            clientView.setCamera(new Image(new ByteArrayInputStream(cameraBytes)));
                        });
                    }
                });
                listView.getItems().add(clientView);
            }
        });

    }

    public void shutdown(){
        System.out.println("client close");
        if (client != null){
            client.close();
        }
    }

}
