package res.schach.figuren;

import java.io.File;

import org.joml.Vector3f;

import engine._3d.Model3d;
import engine._3d.RenderSystem3d;
import res.schach.Schachbrett;
import res.schach.Spielfigur;

public class Laufer extends Spielfigur
{

    public Laufer(Schachbrett brett, boolean owner) 
    {
        super(brett, owner);
        //TODO Auto-generated constructor stub
    }

    static Model3d model = new Model3d(new File("./res/3dModel/Laufer.obj"), RenderSystem3d.getDevice(), new Vector3f(.5f, .5f, .5f));

    boolean[][] mov =
    {
        {false, true , false, true , false},
        {true , false, true , false, true },
        {false, false, false, false, false},
        {true , false, true , false, true },
        {false, true , false, true , false}
    };

    @Override
    public Model3d model()
    {
        return model;
    }

    @Override
    public boolean[][] getMov()
    {
        return mov;
    }
    
}
