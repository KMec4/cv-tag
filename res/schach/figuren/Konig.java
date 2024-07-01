package res.schach.figuren;

import java.io.File;

import org.joml.Vector3f;

import engine._3d.Model3d;
import engine._3d.RenderSystem3d;
import res.schach.Schachbrett;
import res.schach.Spielfigur;

public class Konig extends Spielfigur
{
    public Konig(Schachbrett b, boolean team1)
    {
        super(b, team1);
        //TODO Auto-generated constructor stub
    }

static Model3d model = new Model3d(new File("./res/3dModel/Konig.obj"), RenderSystem3d.getDevice(), new Vector3f(.5f, .5f, .5f));
    boolean[][] mov =
    {
        {true, true , true},
        {true, false, true},
        {true, true , true}
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
