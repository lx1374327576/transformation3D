package unsw.graphics.world;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.TextureRenderer;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Line3D;
import unsw.graphics.geometry.LineStrip2D;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;



/**
 * COMMENT: Comment Game
 *
 * @author malcolmr
 */
public class World extends Application3D implements MouseListener,KeyListener{

    private Terrain terrain;
    private List<Road> roads=new ArrayList<Road>();

    private Shader shader;

    private int segments=64;

    private float pi=(float) 3.1415926535;
    private int times=1;

    private boolean first_or_third=true; ///y
    private boolean if_look_ground=false;///t
    private boolean day_or_night=true;///u
    private boolean road_or_not=true;///i
    private boolean now_road=true;
    private boolean rain_or_not=false;///o
    private boolean lake_or_not=false;///p

    private TriangleMesh lake;

    private int rain_number=0;
    private List<Line3D> list_rain;

    private float dx=(float)0.05;
    private float dy=(float)0.05;
    private float dz=(float)0.05;
    private int[][] direction={{1,0},{0,-1},{-1,0},{0,1}};
    private int now_direction=2;
    private int index = 0;

    private float rotateX = 0;
    private float rotateY = 90;
    private float rotateZ = 0;
    private float r_rotateX = 0;
    private float r_rotateY = 0;
    private Point2D myMousePoint = null;
    private static final float ROTATION_SCALE = 1;

    private Texture texture_tree;
    private Texture texture_model;
    private Texture texture1;
    private Texture texture2;
    private Texture texture3;
    private Texture texture4;

    private TriangleMesh tree; //tree model
    private TriangleMesh model;//person

    private List<TriangleMesh> meshes =  new ArrayList<>();

    public World(Terrain terrain)  throws IOException {
    	super("Assignment 2", 800, 800);
        this.terrain = terrain;
        tree = new TriangleMesh("res/models/tree.ply", true, true); //load the tree model
        model = new TriangleMesh("res/models/bunny.ply", true, true);
        super.setBackground(Color.black);

    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException ,IOException  {
        Terrain terrain = LevelIO.load(new File("res/worlds/test1.json"));
        World world = new World(terrain);
        world.start();
    }

	@Override
	public void display(GL3 gl) {
		super.display(gl);
		if (index == 60){
			index = 0;
		}
		index++;

       // shader.use(gl);
		Shader.setPenColor(gl, Color.WHITE);
		Shader.setPoint3D(gl, "lightPos", new Point3D((float)(0+dx),(float)(0.3+dy),(float)(0+dz)));
        if (day_or_night){
        	Shader.setColor(gl, "lightIntensity", Color.BLACK);
        	Shader.setColor(gl, "ambientIntensity", new Color(1f, 1f, 1f));
        }else{
        	Shader.setColor(gl, "lightIntensity", Color.WHITE);
        	Shader.setColor(gl, "ambientIntensity", new Color(0f, 0f, 0f));
        }

        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", Color.WHITE);
        Shader.setColor(gl, "diffuseCoeff", new Color(1f, 1f, 1f));
        Shader.setColor(gl, "specularCoeff", new Color(1f, 1f, 1f));
        Shader.setFloat(gl, "phongExp", 16f);

        rotate_dy();
        float qx,qy,qz;
        if (first_or_third){
        	qx=0;
        	qy=(float) -2;
        	qz=0;
        }else{
        	qx=(float) 5;
        	qy=(float) -5;
        	qz=0;
        }
		CoordFrame3D frame = CoordFrame3D.identity()
				.rotateX((float)rotateX)
                .rotateY((float)rotateY)
                .translate((float)(qx-dx),(float) (qy-dy),(float)(qz-dz))
                .scale(times, times, times);
		//System.out.println(dx+" "+dz+" "+now_direction);
		//System.out.println("lx"+rotateX);


		if(index < 15){
			gl.glBindTexture(GL.GL_TEXTURE_2D, texture1.getId());
		}else if(index >= 15 && index <30){
			gl.glBindTexture(GL.GL_TEXTURE_2D, texture2.getId());
		}else if(index >= 30 && index <45){
			gl.glBindTexture(GL.GL_TEXTURE_2D, texture3.getId());
		}else{
			gl.glBindTexture(GL.GL_TEXTURE_2D, texture4.getId());
		}

		if (lake_or_not){
			Shader.setPenColor(gl, Color.WHITE);
			lake.draw(gl,frame);
		}

		gl.glBindTexture(GL.GL_TEXTURE_2D, texture_model.getId());
		for (TriangleMesh mesh : meshes)
            mesh.draw(gl, frame);

		Shader.setPenColor(gl, Color.WHITE);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texture_tree.getId());
		//draw the trees
		for (int i=0;i<terrain.trees.size();i++) {
			float x = terrain.trees.get(i).getPosition().getX();
			float z = terrain.trees.get(i).getPosition().getZ();
			int x1 = (int)x;
			int x2 = (int)x+1;
			int z1 = (int)z;
			int z2 = (int)z+1;
			float y = (float)(terrain.getGridAltitude(x2, z1) + terrain.getGridAltitude(x1, z2))/2;

			CoordFrame3D treeFrame =
	                frame.translate((float)x, (float)y+0.5f, (float)z).scale(0.1f, 0.1f, 0.1f);

			tree.draw(gl, treeFrame);
		}

		CoordFrame3D modelFrame = frame
//				.rotateY(r_rotateY)
				.translate((float)(0+dx),(float)(0.3+dy),(float)(0+dz))
				.scale(5, 5, 5);
		if (!first_or_third)model.draw(gl,modelFrame);

		//System.out.println("tsy"+rotateX);

		List<Line3D> list_road = new ArrayList();
    	float dt = 1.0f/segments;
    	float last_x,last_y,last_z;
    	//System.out.println(roads.size());
    	//System.out.println(roads.get(0).size());
    	if (road_or_not!=now_road){
    		System.out.println("Change Roads!");
    		now_road=road_or_not;
    		roads=execute_roads(terrain.roads());
    	}
    	for(Road road:roads){
    		last_x=road.point(0).getX();
    		last_z=road.point(0).getY();
    		//System.out.println(last_x+" "+last_z);
    		last_y=y_get(last_x,last_z);
    		for(int i = 1; i < segments; i++){
        		float t = i*dt;
        		list_road.add(new Line3D(last_x,last_y,last_z,
        				road.point(t).getX(),y_get(road.point(t).getX(),road.point(t).getY()),
        				road.point(t).getY()));
        		last_x=road.point(t).getX();
        		last_z=road.point(t).getY();
        	}
    	}
    	Shader.setPenColor(gl, Color.BLACK);
    	for (Line3D line3D:list_road) line3D.draw(gl,frame);

    	rain_number++;
		if (rain_number>=60)rain_number-=60;
		if (rain_or_not) {
			list_rain=new ArrayList<Line3D>();
			work_rain(rain_number/10);
			Shader.setPenColor(gl, Color.BLUE);
			for (Line3D line3D:list_rain) line3D.draw(gl,frame);
		}
	}

	private void work_rain(int rain_number) {
		// TODO Auto-generated method stub
		for (int i=0;i<terrain.width;i++){
			for (int j=0;j<terrain.depth;j++){
				list_rain.add(new Line3D(i,(float)(8.0/6*((i+j+rain_number)%6)),j,
						i,(float)(8.0/6*((i+j+rain_number)%6+1)),j));
				list_rain.add(new Line3D((float)(i+0.5),(float)(8.0/6*((i*j+rain_number)%6)),(float)(j+0.5),
						(float)(i+0.5),(float)(8.0/6*((i*j+rain_number)%6-1)),(float)(j+0.5)));
			}
		}
	}

	private float y_get(float dx, float dz) {
		// TODO Auto-generated method stub
		float y1=terrain.altitudes[(int)dx][(int)dz];
		float y2=terrain.altitudes[(int)dx+1][(int)dz+1];
		float ddz=dz-(int)dz;
		float ddx=dx-(int)dx;
		float y3;
		if (ddz>ddx){
			y3=terrain.altitudes[(int)dx][(int)dz+1];
			return (y2-y3)*ddx+(y3-y1)*ddz+y1;
		}else{
			y3=terrain.altitudes[(int)dx+1][(int)dz];
			return (y2-y3)*ddz+(y3-y1)*ddx+y1;
		}
	}

	private void rotate_dy() {
		// TODO Auto-generated method stub
		float y1=terrain.altitudes[(int)dx][(int)dz];
		float y2=terrain.altitudes[(int)dx+1][(int)dz+1];
		float ddz=dz-(int)dz;
		float ddx=dx-(int)dx;
		float y3;
		if (ddz>ddx){
			y3=terrain.altitudes[(int)dx][(int)dz+1];
			dy=(y2-y3)*ddx+(y3-y1)*ddz+y1;
			switch (now_direction){
				case 2:
					rotateX=(float) Math.atan(y3-y2)/pi*180;
					break;
				case 1:
					rotateX=(float) Math.atan(y3-y1)/pi*180;
					break;
				case 0:
					rotateX=(float) Math.atan(y2-y3)/pi*180;
					break;
				case 3:
					rotateX=(float) Math.atan(y1-y3)/pi*180;
					break;
			}
		}else{
			y3=terrain.altitudes[(int)dx+1][(int)dz];
			dy=(y2-y3)*ddz+(y3-y1)*ddx+y1;
			switch (now_direction){
				case 0:
					rotateX=(float) Math.atan(y1-y3)/pi*180;
					break;
				case 3:
					rotateX=(float) Math.atan(y2-y3)/pi*180;
					break;
				case 2:
					rotateX=(float) Math.atan(y3-y1)/pi*180;
					break;
				case 1:
					rotateX=(float) Math.atan(y3-y2)/pi*180;
					break;
			}
		rotateX=-rotateX;
		}
		if (!first_or_third)rotateX=45;
		if (if_look_ground)rotateX+=45;
		//System.out.println(y1+" "+y2+" "+y3+" "+ddx+" "+ddz);
	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);
		model.destroy(gl);
		tree.destroy(gl);
		texture1.destroy(gl);
		texture2.destroy(gl);
		texture3.destroy(gl);
		texture4.destroy(gl);
		texture_tree.destroy(gl);
		texture_model.destroy(gl);


	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);
		tree.init(gl);
		model.init(gl);

		roads=terrain.roads();
		roads=execute_roads(roads);

		texture_tree = new Texture(gl,"res/textures/BrightPurpleMarble.png","png",false);
		texture_model = new Texture(gl,"res/textures/grass.bmp","bmp",false);
		texture1 = new Texture(gl, "res/textures/01.jpg", "jpg", false);
		texture2 = new Texture(gl, "res/textures/02.jpg", "jpg", false);
		texture3 = new Texture(gl, "res/textures/03.jpg", "jpg", false);
		texture4 = new Texture(gl, "res/textures/04.jpg", "jpg", false);
		Shader shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
                "shaders/fragment_tex_phong.glsl");

//		shader = new Shader(gl, "shaders/vertex_tex_3d.glsl",
//                "shaders/fragment_tex_3d.glsl");

		shader.use(gl);
		//Shader.setInt(gl, "tex", 0);

        //gl.glActiveTexture(GL.GL_TEXTURE0);


		getWindow().addMouseListener(this);
		getWindow().addKeyListener(this);



		makeExtrusion(gl);

		System.out.println("Successful init!");
	}



	private List<Road> execute_roads(List<Road> roads2) {
		// TODO Auto-generated method stub
		float step=(float) 0.005;
		List<Road> roads=new ArrayList<Road>();
		for (Road road:roads2){
			roads.add(road);
			List<Point2D> tmp;
			float tmp_number=98;
			if (!road_or_not) tmp_number*=road.width();
			for (int i=1;i<=tmp_number;i++){
				tmp=new ArrayList<Point2D>();
				int j=0;
				for (Point2D point2D:road.points){
					j++;
					if (i%2==0){
						tmp.add(new Point2D(point2D.getX()-step*i,point2D.getY()-step*i));
					}else{
						tmp.add(new Point2D(point2D.getX()+step*i,point2D.getY()+step*i));
					}
				}
				roads.add(new Road((float) road.width(),tmp));
			}
		}
		return roads;
	}

	@Override
    public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        Shader.setProjMatrix(gl, Matrix4.perspective(60, 1, 1, 100));
    }

	@Override
	public void mouseDragged(MouseEvent e) {
		Point2D p = new Point2D(e.getX(), e.getY());

        if (myMousePoint != null) {
            float dx = p.getX() - myMousePoint.getX();
            float dy = p.getY() - myMousePoint.getY();

            // Note: dragging in the x dir rotates about y
            //       dragging in the y dir rotates about x
            rotateY += dx * ROTATION_SCALE;
            rotateX += dy * ROTATION_SCALE;

        }
        myMousePoint = p;
	}

	private void makeExtrusion(GL3 gl){
		List<Point3D> mapShape=new ArrayList<Point3D>();
		for (int i=0;i<terrain.width;i++) {
			for (int j=0;j<terrain.depth;j++){
				mapShape.add(new Point3D(i,terrain.altitudes[i][j],j));
			}
		}
		List<Integer> mapShapeIndices=new ArrayList<Integer>();
		for (int i=0;i<terrain.width-1;i++)
			for (int j=0;j<terrain.depth-1;j++){
				mapShapeIndices.add(i*terrain.depth+j);

				mapShapeIndices.add((i+1)*terrain.depth+j+1);
				mapShapeIndices.add((i+1)*terrain.depth+j);

				mapShapeIndices.add((i+1)*terrain.depth+j+1);
				mapShapeIndices.add(i*terrain.depth+j);
				mapShapeIndices.add(i*terrain.depth+j+1);
			}



		TriangleMesh map = new TriangleMesh(mapShape, mapShapeIndices, true);
		map.init(gl);
		meshes.add(map);
		List<Point3D> lakeShape=new ArrayList<Point3D>();
		lakeShape.add(new Point3D(10,0,14));
		lakeShape.add(new Point3D(10,0,22));
		lakeShape.add(new Point3D(2,0,14));
		List<Integer> lakeShapeIndices=Arrays.asList(0,1,2,0,2,1);
		lake=new TriangleMesh(lakeShape, lakeShapeIndices, true);
		lake.init(gl);
	}





	@Override
	public void mouseMoved(MouseEvent e) {
		 myMousePoint = new Point2D(e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		switch (e.getKeyCode()) {

			case KeyEvent.VK_W:
				dx-=direction[now_direction][0]*0.1;
				dz+=direction[now_direction][1]*0.1;
				if (!(dx<=terrain.width*times-1.1 && dx>=0 && dz<=terrain.depth*times-1.1 && dz>=0)){
					dx+=direction[now_direction][0]*0.1;
					dz-=direction[now_direction][1]*0.1;
				}
				break;
			case KeyEvent.VK_S:
				dx+=direction[now_direction][0]*0.1;
				dz-=direction[now_direction][1]*0.1;
				if (!(dx<=terrain.width*times-1.1 && dx>=0 && dz<=terrain.depth*times-1.1 && dz>=0)){
					dx-=direction[now_direction][0]*0.1;
					dz+=direction[now_direction][1]*0.1;
				}
				break;
			case KeyEvent.VK_A:
				now_direction=(now_direction+3)%4;
				if (first_or_third){
					rotateY-=90;
					if (rotateY<0) rotateY+=360;
				}else{
					r_rotateY-=90;
					if (r_rotateY<0) r_rotateY+=360;
				}
				break;
			case KeyEvent.VK_D:
				now_direction=(now_direction+1)%4;
				if (first_or_third){
					rotateY+=90;
					if (rotateY>=360) rotateY-=360;
				}else{
					r_rotateY+=90;
					if (r_rotateY>=360) r_rotateY-=360;
				}
				break;
			case KeyEvent.VK_Y:
				first_or_third=!first_or_third;
				System.out.println("Change angles!");
				break;
			case KeyEvent.VK_T:
				if_look_ground=!if_look_ground;
				System.out.println("Change look direction!");
				break;
			case KeyEvent.VK_U:
				day_or_night=!day_or_night;
				System.out.println("Change time!");
				break;
			case KeyEvent.VK_I:
				road_or_not=!road_or_not;
				break;
			case KeyEvent.VK_O:
				rain_or_not=!rain_or_not;
				System.out.println("Change weather!");
				break;
			case KeyEvent.VK_P:
				lake_or_not=!lake_or_not;
				System.out.println("Change lake!");
				break;
//			case KeyEvent.VK_1:
//				if ( dy >-100 ) dy -= 0.5;
//				break;
//			case KeyEvent.VK_2:
//				if ( dy < 100 ) dy += 0.5;
//				break;
			default:
				break;

        }
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
  }
