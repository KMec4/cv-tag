package res.schach;

import java.io.File;

import org.joml.Vector3f;

import engine._3d.Vertex;
import engine._3d.pickingSystem.PickSystemI;
import res.schach.figuren.Bauer;
import res.schach.figuren.Dame;
import res.schach.figuren.Konig;
import res.schach.figuren.Laufer;
import res.schach.figuren.Springer;
import res.schach.figuren.Turm;
import engine.GameObject;
import engine._3d.Model3d;
import engine._3d.RenderSystem3d;
import engine._3d.RenderSystemI3d;

public class Schachbrett extends GameObject //implements RenderSystemI3d
{
    private Field[][] brett = new Field[8][8];
    private Field selectedField;

    public final float FIELD_LEN = 20.f;

    Vector3f pos = new Vector3f(-60.0f, 40.0f, 20.0f);

    public static final Vector3f COLOR_SELECTED = new Vector3f( .0f, 1.f, .0f);
    public static final Vector3f COLOR_TARGET = new Vector3f( .75f, .0f, .0f);;
    public static final Vector3f COLOR_MOVEABLE = new Vector3f( .0f, .0f, .125f);

    public Schachbrett()
    {
        generateField(new Vector3f( .75f, .5f, .25f ), new Vector3f(1.f, 1.f, 1.f));
        restoreDefault();
    }
    
    public void restoreDefault()
    {
        for(Field[] row : brett) // punsh all still standing chessman
        {
            for(Field f : row)
            {
                f.punsh();
            }
        }

        brett[0][0].setChessman(new Turm      (this, true));
        brett[0][1].setChessman(new Springer  (this, true));
        brett[0][2].setChessman(new Laufer    (this, true));
        brett[0][3].setChessman(new Dame      (this, true));
        brett[0][4].setChessman(new Konig     (this, true));
        brett[0][5].setChessman(new Laufer    (this, true));
        brett[0][6].setChessman(new Springer  (this, true));
        brett[0][7].setChessman(new Turm      (this, true));

        brett[1][0].setChessman(new Bauer     (this, true));
        brett[1][1].setChessman(new Bauer     (this, true));
        brett[1][2].setChessman(new Bauer     (this, true));
        brett[1][3].setChessman(new Bauer     (this, true));
        brett[1][4].setChessman(new Bauer     (this, true));
        brett[1][5].setChessman(new Bauer     (this, true));
        brett[1][6].setChessman(new Bauer     (this, true));
        brett[1][7].setChessman(new Bauer     (this, true));

        brett[7][0].setChessman(new Turm      (this, false));
        brett[7][1].setChessman(new Springer  (this, false));
        brett[7][2].setChessman(new Laufer    (this, false));
        brett[7][3].setChessman(new Dame      (this, false));
        brett[7][4].setChessman(new Konig     (this, false));
        brett[7][5].setChessman(new Laufer    (this, false));
        brett[7][6].setChessman(new Springer  (this, false));
        brett[7][7].setChessman(new Turm      (this, false));

        brett[6][0].setChessman(new Bauer     (this, false));
        brett[6][1].setChessman(new Bauer     (this, false));
        brett[6][2].setChessman(new Bauer     (this, false));
        brett[6][3].setChessman(new Bauer     (this, false));
        brett[6][4].setChessman(new Bauer     (this, false));
        brett[6][5].setChessman(new Bauer     (this, false));
        brett[6][6].setChessman(new Bauer     (this, false));
        brett[6][7].setChessman(new Bauer     (this, false));
    }

    public void selectFieldByCord(float x, float y)
    {
        int sfxN = (int) (x / FIELD_LEN);
        int sfyN = (int) (y / FIELD_LEN);

        selectField(sfxN, sfyN);
    }

    public void selectField(int x, int y)
    {
        System.out.println("Selected Cord: [ " + x + " , " + y + " ]");

        if(x > brett.length || x < 0 || y > brett[0].length || y < 0) // ist Feld außerhalb?
        {
            if(selectedField != null)
            {
                selectedField.setMove(null);
            }
            selectedField = null; //kein Feld Ausgewählt
        }
        else // Figur innerhalb
        {
            if(brett[x][y].canMove()) // ist das Feld begehbar?
            {
                brett[x][y].setChessman(selectedField.getChessman()); //Spielfigur bewegen
                selectedField.setChessman(null);
                selectedField = brett[x][y];
            }
            else // kann das Feld nicht betreten
            {
                if(selectedField != null)
                {
                    selectedField.setMove(null); // das Feld wurde abgewählt
                }
                if(brett[x][y].getChessman() != null) // steht dort jemand
                {
                    selectedField = brett[x][y];
                    selectedField.setSelected();
                }
                else
                {
                    selectedField = null; //kein Feld Ausgewählt
                }
            }
        }

        showMovOptions();
    }

    private void clearMovOptions()
    {
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                brett[i][j].setMove(null);
            }
        }
    }

    private void showMovOptions()
    {
        if(selectedField != null)
        {
            if(selectedField.getChessman() == null)
            {
                return;
            }
            boolean[][] posible = selectedField.getChessman().getTeamMov();

            int xField = selectedField.x;
            int yField = selectedField.y;

            for(int i = 0; i < 8; i++)
            {
                for(int j = 0; j < 8; j++)
                {
                    int x = i - xField + (posible.length / 2);
                    int y = j - yField + (posible[0].length / 2);
                    if(x >= 0 && x < posible.length && y >= 0 && y < posible[0].length)
                    {
                        if(posible[x][y])
                        {
                            brett[i][j].setMove(selectedField.getChessman());
                        }
                        else
                        {
                            brett[i][j].setMove(null);
                        }
                    }
                    else
                    {
                        brett[i][j].setMove(null);
                    }
                }
            }
        }
        else
        {
            clearMovOptions();
        }
    }
    
    public Vector3f translation()
    {
        return pos;
    }

    public Vector3f scale()
    {
        return new Vector3f(FIELD_LEN, 1.0f, FIELD_LEN);
    }

    public Vector3f rotation()
    {
        return new Vector3f(.0f, .0f, .0f);
    }

    public void generateField(Vector3f c0, Vector3f c1)
    {
        Vertex[] data = new Vertex[6];

        boolean switchC = false;
        int vIndex = 0;

        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                Vector3f color;
                if(switchC)
                {
                    color = c1;
                }
                else
                {
                    color = c0;
                }
                switchC = !switchC;


                Vector3f p1 = new Vector3f( (float) i - .5f, 0.0f, (float) j + .5f);
                Vector3f p2 = new Vector3f( (float) i - .5f, 0.0f, (float) j - .5f);
                Vector3f p3 = new Vector3f( (float) i + .5f, 0.0f, (float) j + .5f);

                Vector3f U = new Vector3f(1f);
                Vector3f V = new Vector3f(1f);

                p2.sub(p1, U);
                p3.sub(p1, V);

                Vector3f normal = new Vector3f(
                ( (U.y() * V.z()) - (U.z() * V.y()) ),
                ( (U.z() * V.x()) - (U.x() * V.z()) ),
                ( (U.x() * V.y()) - (U.y() * V.x()) )
                );

                data[ vIndex++ ] = new Vertex( p1, color, normal );
                data[ vIndex++ ] = new Vertex( p2, color, normal );
                data[ vIndex++ ] = new Vertex( p3, color, normal );
                data[ vIndex++ ] = new Vertex( new Vector3f( (float) i - .5f, 0.0f, (float) j - .5f), color, normal);
                data[ vIndex++ ] = new Vertex( new Vector3f( (float) i + .5f, 0.0f, (float) j - .5f), color, normal);
                data[ vIndex++ ] = new Vertex( new Vector3f( (float) i + .5f, 0.0f, (float) j + .5f), color, normal);

                brett[i][j] = new Field(this, data, i, j);
                vIndex = 0;
            }
            switchC = !switchC;
        }
    }

    static class Field extends GameObject implements RenderSystemI3d, PickSystemI
    {
        Schachbrett sch;
        Spielfigur figur;

        boolean target = false, selected = false, moveable = false;
        int x = 0, y = 0;

        Model3d model3d;
        Vector3f color;

        public Field(Schachbrett brett, Vertex[] data, int x, int y)
        {
            super();
            sch = brett;
            model3d = new Model3d(RenderSystem3d.getDevice(), data);
            color = new Vector3f(1.f, 1.f, 1.f);
            this.x = x;
            this.y = y;
        }

        public void setChessman(Spielfigur spf)
        {
            if(spf != null)
            {
                if(figur != null)
                {
                    punsh();
                }
                spf.setPosition(x, y);
                figur = spf;
            }
            else
            {
                figur = null;
            }
            setColor();
        }

        public Spielfigur getChessman()
        {
            return figur;
        }

        public void setSelected(boolean isSelected)
        {
            selected = isSelected;
            setColor();
        }

        public void setTarget(boolean isTarget)
        {
            target = isTarget;
            setColor();
        }

        public void punsh()
        {
            if(figur != null)
            {
                figur.resetColor();
                figur.punsh();
                figur = null;
            }
        }

        public boolean canMove()
        {
            return selected || target || moveable;
        }

        /**
         * Sets the fields properties to this specific situation
         * @param actor if actor == null, all is reseted
         *              if actor == the figur standing on this field, it is selected
         */
        public void setMove(Spielfigur actor)
        {
            if(actor == null)
            {
                selected = false;
                target = false;
                moveable = false;
            }
            else if(figur == null)
            {
                selected = false;
                target = false;
                moveable = true;
            }
            else if(actor == figur)
            {
                selected = true;
                target = false;
                moveable = false;
            }
            else if(actor.getTeam() == figur.getTeam())
            {
                selected = false;
                target = false;
                moveable = false;
            }
            else
            {
                selected = false;
                target = true;
                moveable = false;
            }
            setColor();
        }

        public void setSelected()
        {
            selected = true;
            target = false;
            setColor();
        }
        
    // 3D stuff________________________________________________________________________

        @Override
        public Vector3f translation()
        {
            return sch.translation();
        }

        @Override
        public Vector3f scale()
        {
            return sch.scale();
        }

        @Override
        public Vector3f rotation()
        {
            return sch.rotation();
        }

        @Override
        public Vector3f color()
        {
            return color;
        }

        @Override
        public Model3d model()
        {
            return model3d;
        }

        @Override
        public void picked()
        {
            sch.selectField(x, y);
        }

        private void setColor()
        {
            color.set(1f);
            if(selected)
            {
                if(figur != null)
                {
                    figur.color.add(COLOR_SELECTED);
                }
                color.mul(COLOR_SELECTED);
            }
            else if(target)
            {
                if(figur != null)
                {
                    figur.color.add(COLOR_TARGET);
                }
                color.mul(COLOR_TARGET);
            }
            else if(moveable)
            {
                color.mul(COLOR_MOVEABLE);
            }
            else
            {
                if(figur != null)
                {
                    figur.resetColor();
                }
            }
        }
    }
}
