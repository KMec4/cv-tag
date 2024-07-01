package res.schach;

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
    private Spielfigur[][] brett = new Spielfigur[8][8];
    private boolean[][] canMove = new boolean[8][8];

    public final float FIELD_LEN = 20.f;
    private int sfx = -1, sfy = -1;

    Vector3f pos = new Vector3f(-60.0f, 40.0f, 20.0f);

    public Schachbrett()
    {
        restoreDefault();

        generateField(new Vector3f( .75f, .5f, .25f ), new Vector3f(1.f, 1.f, 1.f));
        //generateField(new Vector3f( .75f, .5f, .25f ), new Vector3f(1.f, 1.f, 1.f));
    }
    
    public void restoreDefault()
    {
        brett[0][0] = new Turm      (this, true).setPosition(0, 0);
        brett[0][1] = new Springer  (this, true).setPosition(0, 1);
        brett[0][2] = new Laufer    (this, true).setPosition(0, 2);
        brett[0][3] = new Dame      (this, true).setPosition(0, 3);
        brett[0][4] = new Konig     (this, true).setPosition(0, 4);
        brett[0][5] = new Laufer    (this, true).setPosition(0, 5);
        brett[0][6] = new Springer  (this, true).setPosition(0, 6);
        brett[0][7] = new Turm      (this, true).setPosition(0, 7);

        brett[1][0] = new Bauer     (this, true).setPosition(1, 0);
        brett[1][1] = new Bauer     (this, true).setPosition(1, 1);
        brett[1][2] = new Bauer     (this, true).setPosition(1, 2);
        brett[1][3] = new Bauer     (this, true).setPosition(1, 3);
        brett[1][4] = new Bauer     (this, true).setPosition(1, 4);
        brett[1][5] = new Bauer     (this, true).setPosition(1, 5);
        brett[1][6] = new Bauer     (this, true).setPosition(1, 6);
        brett[1][7] = new Bauer     (this, true).setPosition(1, 7);

        brett[7][0] = new Turm      (this, false).setPosition(7, 0);
        brett[7][1] = new Springer  (this, false).setPosition(7, 1);
        brett[7][2] = new Laufer    (this, false).setPosition(7, 2);
        brett[7][3] = new Dame      (this, false).setPosition(7, 3);
        brett[7][4] = new Konig     (this, false).setPosition(7, 4);
        brett[7][5] = new Laufer    (this, false).setPosition(7, 5);
        brett[7][6] = new Springer  (this, false).setPosition(7, 6);
        brett[7][7] = new Turm      (this, false).setPosition(7, 7);

        //brett[6][0] = new Bauer     (this, true).setPosition(6, 0);
        //brett[6][1] = new Bauer     (this, true).setPosition(6, 1);
        //brett[6][2] = new Bauer     (this, true).setPosition(6, 2);
        //brett[6][3] = new Bauer     (this, true).setPosition(6, 3);
        //brett[6][4] = new Bauer     (this, true).setPosition(6, 4);
        //brett[6][5] = new Bauer     (this, true).setPosition(6, 5);
        //brett[6][6] = new Bauer     (this, true).setPosition(6, 6);
        //brett[6][7] = new Bauer     (this, true).setPosition(6, 7);
    }

    public void selectFieldByCord(float x, float y)
    {
        int sfxN = (int) (x / FIELD_LEN);
        int sfyN = (int) (y / FIELD_LEN);

        selectField(sfxN, sfyN);
    }

    public void selectField(int x, int y)
    {
        int sfxN = x;
        int sfyN = y;

        if(sfx > -1 && brett[sfx][sfy] != null) // alte Spielfigur zurücksetzen
        {
            brett[sfx][sfy].resetColor();
        }

        if(sfxN > brett.length || sfxN < 0 || sfyN > brett[0].length || sfyN < 0) // sollte neues Feld außerhalb liegen, wird das alte abgewählt
        {
            sfx = -1;
            sfy = -1;
            return;
        }

        if(sfx > -1 && canMove[sfxN][sfyN]) // neue figur auswählen und bewegen
        {

            if(brett[sfxN][sfyN] != null) // figuren schlagen
            {
                brett[sfxN][sfyN].punsh();
            }
            brett[sfxN][sfyN] = brett[sfx][sfy].setPosition(sfxN, sfyN);
            brett[sfx][sfy] = null;
            clearMovOptions();
        }
        else
        {
            if(brett[sfxN][sfyN] != null) // figur auswälen
            {
                brett[sfxN][sfyN].color.add(.0f, 1.f, .0f);
                sfx = sfxN;
                sfy = sfyN;
                showMovOptions();
            }
        }
        
    }

    private void clearMovOptions()
    {
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                canMove[i][j] = false;
            }
        }
    }

    private void showMovOptions()
    {
        if(brett[sfx][sfy] != null)
        {
            boolean[][] posible = brett[sfx][sfy].getTeamMov();

            for(int i = 0; i < 8; i++)
            {
                for(int j = 0; j < 8; j++)
                {
                    int x = i - sfx + (posible.length / 2);
                    int y = j - sfy + (posible[0].length / 2);
                    if(x >= 0 && x < posible.length && y >= 0 && y < posible[0].length)
                    {
                        canMove[i][j] = posible[x][y];
                    }
                    else
                    {
                        canMove[i][j] = false;
                    }
                    
                    if(canMove[i][j] && brett[i][j] != null)
                    {
                        System.out.print(" X");
                    }
                    else if (canMove[i][j])
                    {
                        System.out.print( " #");
                    }
                    else if ( brett[i][j] != null )
                    {
                        System.out.print(" S");
                    }
                    else
                    {
                        System.out.print(" O");
                    }
                    
                }
                System.out.println();
            }
        }
    }

    // 3D stuff________________________________________________________________________
    
    Model3d model;

    //@Override
    public Vector3f translation()
    {
        return pos;
    }

    //@Override
    public Vector3f scale()
    {
        return new Vector3f(FIELD_LEN, 1.0f, FIELD_LEN);
    }

    //@Override
    public Vector3f rotation()
    {
        return new Vector3f(.0f, .0f, .0f);
    }

    //@Override
    public Vector3f color()
    {
        return new Vector3f(1.0f, 1.0f, 1.0f);
    }

    //@Override
    public Model3d model()
    {
        return model;
    }

    public void generateField(Vector3f c0, Vector3f c1)
    {
        Vertex[] data = new Vertex[6]; //[8*8*6];

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

                new Field(this, data, i, j);
                vIndex = 0;
            }
            switchC = !switchC;
        }
    }

    static class Field extends GameObject implements RenderSystemI3d, PickSystemI
    {
        Schachbrett sch;
        Model3d model3d;
        Vector3f c;
        int x = 0, y = 0;

        public Field(Schachbrett brett, Vertex[] data, int x, int y)
        {
            super();
            sch = brett;
            model3d = new Model3d(RenderSystem3d.getDevice(), data);
            c = new Vector3f(1.f, 1.f, 1.f);
            this.x = x;
            this.y = y;
        }

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
            return c;
        }

        @Override
        public Model3d model()
        {
            return model3d;
        }

        public void setColor(Vector3f color)
        {
            this.c = color;
        }

        @Override
        public void picked()
        {
            System.out.println("Selected Cord: [ " + x + " , " + y + " ]");
            sch.selectField(x, y);
        }
        
    }

}
