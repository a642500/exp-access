/*
 *     Copyright (C) 2016 Carlos
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package me.toxz.exp.dac.fx;

import com.j256.ormlite.dao.Dao;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.toxz.exp.dac.data.DatabaseHelper;
import me.toxz.exp.dac.data.model.*;
import me.toxz.exp.dac.fx.animation.ShakeTransition;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Carlos on 1/6/16.
 */
public class GrantDialogController implements Initializable {
    private static Stage mStage;
    private static Consumer<AccessRecord> mResultCallback;
    @FXML ComboBox<User> userComboBox;
    @FXML ComboBox<MObject> objectComboBox;
    @FXML ChoiceBox<AccessType> permissionChoiceBox;
    @FXML Label headLabel;

    public static void show(Consumer<AccessRecord> resultCallback) throws IOException {
        mResultCallback = resultCallback;
        mStage = new Stage();
        mStage.initStyle(StageStyle.UTILITY);

        Parent parent = FXMLLoader.load(GrantDialogController.class.getClassLoader().getResource("GrantDialog.fxml"));
        Scene scene = new Scene(parent, 400, 300);
        mStage.setScene(scene);
        mStage.setTitle("Grant");
        mStage.show();
    }

    public static void dismiss(AccessRecord record) {
        mStage.close();
        mStage = null;
        if (mResultCallback != null) {
            mResultCallback.accept(record);
        }
    }

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            User user = Main.getLoginUser();
            List<User> grantable = DatabaseHelper.getUserDao().queryForAll();
            grantable.remove(User.admin());
            grantable.remove(user);
            userComboBox.getItems().addAll(grantable);
            userComboBox.getSelectionModel().select(0);

            final Dao<AccessRecord, Integer> accessRecordDao = DatabaseHelper.getAccessRecordDao();
            List<AccessRecord> controllable = accessRecordDao.queryForMatching(new AccessRecord(user, null, AccessType.CONTROL, null));
            objectComboBox.getItems().addAll(controllable.stream().map(AccessRecord::getObject).collect(Collectors.toList()));

            objectComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    List<AccessRecord> records = accessRecordDao.queryForMatching(new AccessRecord(user, newValue, null, null));
                    List<AccessType> types = records.stream().map(AccessRecord::getAccessType).collect(Collectors.toList());
                    //                    types.remove(AccessType.CONTROL);// type1: center

                    permissionChoiceBox.getItems().setAll(types);
                    permissionChoiceBox.getSelectionModel().select(0);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void cancel(ActionEvent actionEvent) {
        dismiss(null);
    }

    public void ok(ActionEvent actionEvent) {
        User user = userComboBox.getSelectionModel().getSelectedItem();
        MObject object = objectComboBox.getSelectionModel().getSelectedItem();
        AccessType type = permissionChoiceBox.getSelectionModel().getSelectedItem();

        AccessRecord record = new AccessRecord(user, object, type, Main.getLoginUser());

        try {
            BlackToken token = new BlackToken(user, object, type);
            if (DatabaseHelper.getBlackTokenDao().queryForMatching(token).size() > 0) {
                headLabel.setText("No! Black Token forbid.");
                new ShakeTransition(mStage.getScene().getRoot(), event -> {
                }).playFromStart();
                return;
            }

            List<AccessRecord> accessRecords = DatabaseHelper.getAccessRecordDao().queryForMatching(record);

            if (accessRecords.size() > 0) {
                headLabel.setText("Repeat grant!");
                new ShakeTransition(mStage.getScene().getRoot(), event -> {
                }).playFromStart();

                //TODO check circle
            } else if (checkCircle(record)) {
                headLabel.setText("Circular grant!");
                new ShakeTransition(mStage.getScene().getRoot(), event -> {
                }).playFromStart();
            } else {
                DatabaseHelper.getAccessRecordDao().create(record);
                dismiss(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkCircle(AccessRecord access) throws SQLException {
        final User target = access.getSubject();
        final MObject object = access.getObject();
        final User origin = access.getGrantedUser();
        final AccessType type = access.getAccessType();

        if (target.equals(origin)) {
            return true;
        } else {
            List<AccessRecord> parent = DatabaseHelper.getAccessRecordDao().queryForMatching(new AccessRecord(origin, object, type, null));
            for (AccessRecord record : parent) {
                if (record.getGrantedUser().equals(target)) return true;
                else if (checkCircle(record)) return true;
            }
            return false;
        }
    }
}
