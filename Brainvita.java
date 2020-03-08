import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;

//<applet code="Brainvita" width=300 height=300>
//</applet>

public class Brainvita extends Applet implements ActionListener,Runnable
{
	int pickX=-1,pickY=-1,pointX=-1,pointY=-1,dragX=-1,dragY=-1;
	public int board[];
	public int i,j,k,deadBalls;
	public Button restart=new Button("RESTART");
	public Button bsol=new Button("DEMO");
	public Button inst=new Button("INSTRUCTIONS");
	public Button back=new Button("BACK");
	public Button undo=new Button("UNDO");
	boolean instructions;
	public boolean gameend;
	final int UP=1,RIGHT=2,DOWN=3,LEFT=4;
	public int solution[][]={
		{3,5,1},{1,4,2},{3,3,3},{3,6,1},{2,6,1},{3,4,4},{5,4,4},{4,6,1},
		{3,4,2},{0,4,2},{2,3,3},{0,3,2},{2,2,3},{2,5,1},{0,2,2},{6,4,4},
        {4,3,3},{6,3,4},{4,2,3},{4,5,1},{6,2,4},{3,2,2},{4,0,3},{4,3,1},
        {2,0,2},{4,0,3},{5,2,4},{3,1,3},{3,3,4},{2,1,3},{1,3,2}};
	Thread t=null;
	int count;
	Stack u=new Stack();
	Integer o=new Integer(0);

	//INITIALISATION OF THE APPLET
	public void init()
	{
		setLayout(null);
		resize(4000,1000);
		board=new int[49];
		t=new Thread(this);
		fillBoard();
		addComp(restart,280,136,100,50,true);
		addComp(bsol,280,73,100,50,true);
		addComp(inst,280,10,100,50,true);
		addComp(back,900,800,100,50,false);
		addComp(undo,280,199,100,50,true);
		repaint();
	}

	//ADD A BUTTON
	public void addComp(Button b,int x,int y,int w,int h,boolean vis)
	{
		b.setBounds(x,y,w,h);
		b.setVisible(vis);
		add(b);
		b.addActionListener(this);
	}

	//INITIALISE THE BOARD
	public void fillBoard()
	{
		for(i=0;i<49;i++)
			board[i]=1;
		for(i=0;i<2;i++)
			for(j=0;j<2;j++)
			{
				board[i*7+j]=-1;
				board[i*7+j+5]=-1;
				board[i*7+j+35]=-1;
				board[i*7+j+40]=-1;
			}
		board[24]=0;
		undo.setEnabled(false);
		bsol.setEnabled(true);
		gameend=false;
		instructions=false;
		deadBalls=0;
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource()==restart)
		{
			u=new Stack();
			if(t.isAlive())
				t.stop();
			fillBoard();
			repaint();
		}

		if(ae.getSource()==bsol)
		{
			bsol.setEnabled(false);
			undo.setEnabled(false);
			displaysol();
		}

		if(ae.getSource()==inst)
		{
			restart.setVisible(false);
			bsol.setVisible(false);
			inst.setVisible(false);
			back.setVisible(true);
			undo.setVisible(false);
			instructions=true;
			repaint();
		}

		if(ae.getSource()==back)
		{
			restart.setVisible(true);
			bsol.setVisible(true);
			inst.setVisible(true);
			back.setVisible(false);
			undo.setVisible(true);
			instructions=false;
			repaint();
		}

		if(ae.getSource()==undo)
		{
			gameend=false;
			undoMove();
			repaint();
		}
	}

	//POP THE VALUES OF THE PREVIOUS MOVE FROM THE STACK AND UNDO THIS MOVE
	public void undoMove()
	{
		o=(Integer)u.pop();
		pickY=o.intValue();
		o=(Integer)u.pop();
		pickX=o.intValue();
		o=(Integer)u.pop();
		dragY=o.intValue();
		o=(Integer)u.pop();
		dragX=o.intValue();
		board[pickY*7+pickX]=0;
		board[dragY*7+dragX]=1;
		board[((pickY+dragY)/2)*7+(pickX+dragX)/2]=1;
		deadBalls--;
		if(u.empty())
		{
			undo.setEnabled(false);
			bsol.setEnabled(true);
		}
	}

	public void fixBox(Graphics g,int x,int y,int w,int h,Color c)
	{
		g.setColor(Color.red);
		g.fillRect(x,y,w,h);
	}

	public void fixDisc(Graphics g,int x,int y,int r,Color c)
	{
		g.setColor(c);
		g.fillOval(x,y,r,r);
	}

	public void fixCircle(Graphics g,int x,int y,int r,Color c)
	{
		g.setColor(c);
		g.drawOval(x,y,r,r);
	}

	//DRAW A BALL ON THE BOARD AT THE SPECIFIED LOCATION
	public void boardBall(Graphics g,int x, int y)
	{
		fixDisc(g,x+3,y+3,60,Color.black);
		fixDisc(g,x,y,60,Color.green);
	}

	//DRAW AN EMPTY SPACE AT THE SPECIFIED LOCATION
	public void boardHole(Graphics g,int x,int y)
	{
		g.setColor(Color.white);
		fixDisc(g,x,y,60,Color.black);
	}

	//DRAW A BALL THAT HAS BEEN REMOVED
	public void deadBall(Graphics g,int x,int y)
	{
		fixDisc(g,x+1,y+1,62,Color.black);
		fixDisc(g,x,y,60,Color.green);
	}

	//PERFORM THE ACTION WHEN THE MOUSE IS PRESSED DOWN
	public boolean mouseDown(Event e,int x,int y)
	{
		pointX=-1;
		pointY=-1;
		pickX=-1;
		pickY=-1;
		dragX=-1;
		dragY=-1;

		//CALCULATE THE X AND Y INDEX OF THE POSITION ON THE BOARD
		i=(y-160)/100;
		j=(x-620)/100;

		//IF IT IS A VALID POSITION
		if((i>-1) && (i<7) && (j>-1) && (j<7))
		{
			pointX=x;
			pointY=y;
			pickX=j;
			pickY=i;
		}
		return false;
	}

	//PERFORM THE ACTION WHEN THE MOUSE IS DRAGGED
	public boolean mouseDrag(Event e,int x,int y)
	{
		//IF MOUSE IS DRAGGED ALONG THE Y-AXIS
		//if((x<pointX+49) && (x>pointX-49))
		//{
			//IF MOUSE IS DRAGGED UP
			if((pickY>1) && (y<pointY-60))
			{
				//IF BALL JUMP IS VALID
				if((board[pickY*7+pickX-7]==1) && (board[pickY*7+pickX-14]==0))
				{
					dragX=pickX;
					dragY=pickY-2;
				}
			}

			//IF MOUSE IS DRAGGED DOWN
			else if((pickY<5) && (y>pointY+60))
			{
				//IF BALL JUMP IS VALID
				if((board[pickY*7+pickX+7]==1) && (board[pickY*7+pickX+14]==0))
				{
					dragX=pickX;
					dragY=pickY+2;
				}
			}
	//	}
		

		//IF MOUSE IS DRAGGED ALONG THE X AXIS
		//else if((y<pointY+49) && (y>pointY-49))
		//{
			//IF MOUSE IS DRAGGED TO THE LEFT
			if((pickX>1) && (x<pointX-60))
			{
				//IF BALL JUMP IS VALID
				if((board[pickY*7+pickX-1]==1) && (board[pickY*7+pickX-2]==0))
				{
					dragX=pickX-2;
					dragY=pickY;
				}
			}

			//IF MOUSE IS DRAGGED TO THE RIGHT
			else if((pickX<5) && (x>pointX+60))
			{
				//IF BALL JUMP IS VALID
				if((board[pickY*7+pickX+1]==1) && (board[pickY*7+pickX+2]==0))
				{
					dragX=pickX+2;
					dragY=pickY;
				}
			}
		//}
		return false;
	}

	//PERFORM THE ACTION WHEN THE MOUSE IS RELEASED
	public boolean mouseUp(Event e,int x,int y)
	{
		//IF MOUSE WAS DRAGGED TO A VALID POSITION
		if(dragX>-1)
		{
			//PERFORM THE BALL JUMP OPERATION
			undo.setEnabled(true);
			board[pickY*7+pickX]=0;
			board[dragY*7+dragX]=1;
			board[7*(pickY+dragY)/2+(pickX+dragX)/2]=0;
			deadBalls++;
			bsol.setEnabled(false);
			if(gameover())
				gameend=true;

			//PUSH THE VALUES OF pickX,pickY,dragX,
			//dragY ONTO THE STACK TO IMPLEMENT UNDO
			Integer pX=new Integer(pickX);
			u.push(pX);
			Integer pY=new Integer(pickY);
			u.push(pY);
			Integer dX=new Integer(dragX);
			u.push(dX);
			Integer dY=new Integer(dragY);
			u.push(dY);
			repaint();
		}
		return false;
	}

	public void update(Graphics g)
	{
		paint(g);
	}

	//CHECKS IF THE GAME IS OVER
	public boolean gameover()
	{
		for(i=2;i<5;i++)
			for(j=2;j<5;j++)
			{
				if(board[i*7+j]==1)
				{
					if((board[i*7+j-7]==1) && (board[i*7+j-14]==0))
						return false;
					if((board[i*7+j-1]==1) && (board[i*7+j-2]==0))
						return false;
					if((board[i*7+j+7]==1) && (board[i*7+j+14]==0))
						return false;
					if((board[i*7+j+1]==1) && (board[i*7+j+2]==0))
						return false;
				}
				else
				{
					if((board[i*7+j-7]==1) && (board[i*7+j-14]==1))
						return false;
					if((board[i*7+j-1]==1) && (board[i*7+j-2]==1))
						return false;
					if((board[i*7+j+7]==1) && (board[i*7+j+14]==1))
						return false;
					if((board[i*7+j+1]==1) && (board[i*7+j+2]==1))
						return false;
				}

			}

		for(i=0;i<2;i++)
		{
			if((board[i*7+2]==1) && (board[i*7+3]==1) && (board[i*7+4]==0))
				return false;
			if((board[i*7+2]==0) && (board[i*7+3]==1) && (board[i*7+4]==1))
				return false;
			if((board[i*7+2+35]==1) && (board[i*7+3+35]==1) && (board[i*7+4+35]==0))
				return false;
			if((board[i*7+2+35]==0) && (board[i*7+3+35]==1) && (board[i*7+4+35]==1))
				return false;
			if((board[2*7+i]==1) && (board[3*7+i]==1) && (board[4*7+i]==0))
				return false;
			if((board[2*7+i]==0) && (board[3*7+i]==1) && (board[4*7+i]==1))
				return false;
			if((board[2*7+i+5]==1) && (board[3*7+i+5]==1) && (board[4*7+i+5]==0))
				return false;
			if((board[2*7+i+5]==0) && (board[3*7+i+5]==1) && (board[4*7+i+5]==1))
				return false;
		}

		return true;
	}

	//PAINT FUNCTION DISPLAYS THE GAME
	public void paint(Graphics g)
	{
		Font myFont = new Font ("Courier New", 1,50);
		g.setFont(myFont);
		showStatus("Welcome to BrainVita Game.");
		g.setColor(Color.black);
		g.fillRect(0,0,size().width,size().height);

		//IF USER DOES NOT WANT TO VIEW THE INSTRUCTIONS
		if(instructions==false)
		{
			//DISPLAY THE BOARD
			fixDisc(g,500,50,900,Color.gray);
			fixDisc(g,500,50,900,Color.orange);
			//fixCircle(g,575,125,750,Color.black);

			//IF GAME IS OVER DISPLAY THE SCORE
			if(gameend)
			{
				g.setColor(Color.black);
				g.drawString("Game Over!",775,450);
				g.drawString("Your Score is--"+(32-deadBalls),700,500);
				if(32-deadBalls==1)
					g.drawString("CONGRATULATIONS",900,450);
			}

			else
			{
				//DISPLAY THE BALLS IN THE APPROPRIATE POSITION
				for (i=0;i<7;i++)
					for (j=0;j<7;j++)
						if (board[i*7+j]>-1)
							if (board[i*7+j]==0)
								boardHole(g,620+j*100,160+i*100);
							else
								boardBall(g,620+j*100,160+i*100);

				//THE ELIMINATED BALLS OUTSIDE THE BOARD
				for (i=0;i<deadBalls;i++)
				{
					if(i<8)
						deadBall(g,5+i*63,250);
					else if((i>=8)&&(i<16))
						deadBall(g,5+(i-8)*63,320);
					else if((i>=16)&&(i<24))
						deadBall(g,5+(i-16)*63,390);
					else
						deadBall(g,5+(i-24)*63,460);
				}
			}
		}

		//DISPLAY THE INSTRUCTION
		else
		{
			displayinst(g);
		}
	}

	//CRAETE A NEW THREAD TO FIND OPTIMAL SOLUTION
	public void displaysol()
	{
		t=new Thread(this);
		t.start();
	}

	//THREAD RUN FUNCTION TO CALCULATE THE OPTIMAL SOLUTION
	public void run()
	{
		for(count=0;count<solution.length;count++)
		{
			int X=-1,Y=-1;
			board[solution[count][1]*7+solution[count][0]]=0;
			switch(solution[count][2])
			{
				case UP:
					Y=solution[count][1]-2;
					X=solution[count][0];
					break;
				case DOWN:
					Y=solution[count][1]+2;
					X=solution[count][0];
					break;
				case RIGHT:
					Y=solution[count][1];
					X=solution[count][0]+2;
					break;
				case LEFT:
					Y=solution[count][1];
					X=solution[count][0]-2;
					break;
			}
			deadBalls+=1;
			board[Y*7+X]=1;
			board[((solution[count][1]+Y)/2)*7+(solution[count][0]+X)/2]=0;
			repaint();

			//INSERT A 1.5 SEC DELAY
			try
			{
				Thread.sleep(1500);
			}
			catch(InterruptedException e)	
			{
				System.out.println("Interrupted");
			}
		}
		gameend=true;
		t.stop();
	}

	//DISPLAY THE INSTRUCTIONS
	public void displayinst(Graphics g)
	{
		g.setColor(Color.white);
		Font myFont = new Font ("Courier New", 1,25);
		g.setFont(myFont);
		g.drawString("Welome to Brainvita--A marble game",700,65);
		g.drawString("**********OBJECTIVE**********",400,140);
		g.drawString("You have to eliminate as many marbles as possible. Select a marble",450,170);
		g.drawString("and jump it over an adjecent marble to remove it. (the middle marble)",450,195);
		g.drawString("*********INSTRUCTIONS********",400,250);
		g.drawString("Drag a marble using the mouse to jump it over another marble such that there",450,280);
		g.drawString("is only one marble betweeen the hole and the marble in motion.",450,305);
		g.drawString("You can only move in Vertical and Horizontal directions.",450,330);
	
		g.drawString("************SCORE************",400,385);
		g.drawString("Your score is the number of marbles remaining after the game is over.",450,415);
		g.drawString("above 6--Try Harder",450,435);
		g.drawString("6--Better luck next time",450,455);
		g.drawString("5--Average",450,475);
		g.drawString("4--Just above average",450,495);
		g.drawString("3--Good work",450,515);
		g.drawString("2--Almost there",450,535);
		g.drawString("1--Pure Genius",450,555);
	}
}   