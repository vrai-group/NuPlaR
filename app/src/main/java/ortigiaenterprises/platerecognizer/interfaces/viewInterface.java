package ortigiaenterprises.platerecognizer.interfaces;

import ortigiaenterprises.platerecognizer.OcrRes;

/**
 * Created by DionisioII on 19/07/2016.
 */
public interface viewInterface {
    void hideView();

    void Save(OcrRes valueToSave);

    void Delete(OcrRes valueToDelete);

    void Info(String NumberPlate,String DateTime);
}
