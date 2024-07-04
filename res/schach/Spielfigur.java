package res.schach;

import org.joml.Vector3f;

import engine.GameObject;
import engine._3d.RenderSystemI3d;
import engine._3d.pickingSystem.PickSystemI;
import engine.systems.MovingSystemI;
import engine.systems.RotationSystemI;

public abstract class Spielfigur extends GameObject implements RenderSystemI3d, PickSystemI, MovingSystemI, RotationSystemI
{
    static final Vector3f COLOR_TEAM_WHITE = new Vector3f(1f, 1f, 1f);
    static final Vector3f COLOR_TEAM_BLACK = new Vector3f(0.75f, .0f, .5f);
    public Vector3f color;

    public boolean teamW = true;

    public Vector3f position;
    public Vector3f scale;
    public Vector3f direction;

    Schachbrett schBrett;

    int x = 0, y = 0;

    public Spielfigur(Schachbrett brett, boolean owner)
    {
        teamW = owner;
        if(owner)
        {
            color = new Vector3f(COLOR_TEAM_WHITE);
        }
        else
        {
            color = new Vector3f(COLOR_TEAM_BLACK);
        }

        schBrett = brett;
        position = new Vector3f(.0f, .0f, 10.0f);
        direction = new Vector3f(.0f, .0f, 1.0f);
        scale = new Vector3f(1f, 1f,1f);
    }

    public boolean getTeam()
    {
        return teamW;
    }

    public abstract boolean[][] getMov(); // x und y sind ungerade, da die mitte die Figur ist!;

    public boolean[][] getTeamMov()
    {
        return getMov();
    }

    public Spielfigur setPosition(int x, int y)
    {
        position = new Vector3f(x * schBrett.FIELD_LEN, .0f, y * schBrett.FIELD_LEN).add(schBrett.translation());
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public Vector3f translation()
    {
        return position;
    }

    @Override
    public Vector3f scale()
    {
        return scale;
    }

    Vector3f rot = new Vector3f( 3.141f, 0.0f, .0f);
    @Override
    public Vector3f rotation()
    {
        return rot;
    } 

    @Override
    public Vector3f color()
    {
        return color;
    }

    @Override
    public void picked()
    {
        schBrett.selectField(x, y);
    }

    public void resetColor()
    {
        if(teamW)
        {
            color = new Vector3f(COLOR_TEAM_WHITE);
        }
        else
        {
            color = new Vector3f(COLOR_TEAM_BLACK);
        }
    }

    // Moving System stuff
    float speed = 0f;
    Vector3f movingDir = new Vector3f(.0f, .0f, .0f);
    Vector3f rotSpeed = new Vector3f(.0f, .0f, .0f);

    public void punsh()
    {
        speed = 0.1f;
        movingDir.add(.5f, -0.5f, .25f);
        rotSpeed.add(.001f, .005f, .001f);
    }

    @Override
    public Vector3f getDirection()
    {
        return movingDir;
    }

    @Override
    public Vector3f getPosition()
    {
        return position;
    }

    @Override
    public float getSpeedM_S()
    {
        return speed;
    }

    @Override
    public Vector3f getRotation()
    {
        return rot;
    }

    @Override
    public Vector3f getRotSpeeds()
    {
        return rotSpeed;
    }

}
