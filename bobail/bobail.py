from tkinter import *
from state import *
from mcts import *
import math
from enum import Enum, auto
import time

def grid_position(a, b):
    return a//100, b//100

def circle_coords(i,j):
    return (j*100+45, i*100+45, j*100+95, i*100+95)


class Application(Frame):

    # -------- Initialisation --------

    def __init__(self, nb_players, ai_thinking_time, player_first = False, master = None):
        """Initialization of all the data"""
        Frame.__init__(self, master)
        self.grid()
        self.bind_all("<Escape>", self._exit)
        self.real_players = self.set_of_players(nb_players, player_first) # real players numbers
        self.ai_thinking_time = ai_thinking_time # time in seconds of thinking for the ai
        self.create_initial_board() 
        self.create_informations(nb_players)
        self.previous_moves = []
        self.state = State()
        self.mcts_root_node = Node(self.state.possible_plays())
        self.update()
        if nb_players == 1:
            self.after(1,self.ai_alone)
        self.after(1,self.initiate_turn)

    def set_of_players(self, nb_players, player_first):
        """Returns {}, {1}, {2} or {1,2} depending on what players are real"""
        if nb_players == 0:
            return []
        elif nb_players == 2:
            return [1,2]
        elif player_first: # = 1
            return [1]
        else:
            return [2]
    
    def create_initial_board(self):
        """Creates the elements of the graphical board"""
        self.canv = Canvas(self, height = 500, width = 500, relief="ridge",borderwidth=20)
        self.canv.grid(column = 0, columnspan = 5, row = 0, rowspan = 5)
        self.canv.bind("<Button-1>", self._click)
        for i in [50, 150, 250, 350, 450]: # The dots where we can play
            for j in [50, 150, 250, 350, 450]:
                self.canv.create_oval(i+10, j+10, i+30, j+30, fill="black")
        self.pieces = {(i,j):None for i in range(5) for j in range(5)}
        for j in range(5): # The pieces
            self.pieces[(0,j)] = self.canv.create_oval(*circle_coords(0,j), width = 6, outline = "grey", fill = "white")
            self.pieces[(4,j)] = self.canv.create_oval(*circle_coords(4,j), width = 6, outline = "grey", fill = "black")
        self.pieces[(2,2)] = self.canv.create_oval(*circle_coords(2,2), width = 6, outline = "grey", fill = "red") # the bobail
        self.possible_moves_circle_ids = [] # empty list that will be used for the green circle of possible moves
        self.is_moving = False # to prevent from doing ai_plays when a pawn is moving, otherwise it makes some lags (the pawn plays too slowly)
        self.doing_undo = False # prevent from undoing when it is currently undoing
        self.clicked_pawn = None # the current clicked pawn to remember when playing a pawn
        self.is_won = False # To prevent from doing computations when game is finished

    def create_informations(self, nb_players):
        """Creates the information of the player color, and the undo button"""
        self.current_player_label = Label(self, height=3, width=8, font=(None,15,"bold"), text="Player", bg="black", fg="white", relief="raised", borderwidth=15) # player color
        self.current_player_label.grid(column = 5, row = 0)
        if nb_players != 0: # undo button
            self.button = Button(self, height=3, width=6, text="Undo", font=(None,15,"bold"), borderwidth=10, command=self._undo)
            self.button.grid(column=5, row=1)

    # -------- Main function to do moves --------

    def play_on_data_and_graphic(self, move):
        """Play a move everywhere that needs to be updated and checks the winner"""
        self.previous_moves.append(move)
        self.move_piece(move)
        self.play_on_data(move)

    def unplay_on_data_and_graphic(self):
        """Unplay a move everywhere that needs to be updated"""
        move = self.previous_moves.pop()
        self.move_piece(move[::-1])
        self.unplay_on_data(move)
            
    # -------- Play on data --------
    
    def play_on_data(self, move):
        """Play the move on the data, ie the state and the mcts, and checks the winner"""
        winner = self.state.play(move)
        self.check_winner(winner)
        self.update_mcts_root_node(move)

    def check_winner(self, winner):
        """Checks if there is a winner, and stops the program if there is one"""
        if winner:
            print(["Black","White"][winner-1], "wins")
            self.is_won = True
            self.after(1000,self.quit)
        if len(self.state.possible_plays()) == 0:
            print(["Black","White"][2-self.state.player], "wins")
            self.is_won = True
            self.after(1000,self.quit)

    def update_mcts_root_node(self, move):
        """Update the mcts root node by playing a move"""
        self.mcts_root_node = self.mcts_root_node.child_with_move(move)
        self.mcts_root_node = Node(self.state.possible_plays())

    def unplay_on_data(self, move):
        """Unplay the move on the data"""
        self.state.unplay(move)
        self.mcts_root_node = Node(self.state.possible_plays())

            
    # -------- Graphical moves --------
            
    def add_possible_moves(self, moves):
        """Adds all the possible moves to the circle list (green circles representing the possible moves)"""
        for start, end in moves:
            i,j = end
            self.possible_moves_circle_ids.append(self.canv.create_oval(100*j+40, 100*i+40, 100*j+100, 100*i+100, width = 10, outline = "green"))
        self.update()

    def remove_possible_moves(self):
        """Removes the circles created for the possible moves"""
        for i in self.possible_moves_circle_ids:
            self.canv.delete(i)
        self.possible_moves_circle_ids = []

    def move_piece(self, move):
        """Moves the piece graphically"""
        self.remove_possible_moves()
        start, end = move
        if self.pieces[start] == None or self.pieces[end] != None:
            raise ValueError("Wrong move given to move piece")
        piece_id = self.pieces[start]
        self.pieces[end] = piece_id
        self.pieces[start] = None
        self.smooth_move(piece_id,circle_coords(*start),circle_coords(*end))

    def smooth_move(self, piece_id, start, end):
        """Makes a smooth move for the piece"""
        self.is_moving = True
        for t in range(1,100):
            v = (math.sin(t/100*math.pi-math.pi/2)+1)/2
            self.canv.coords(piece_id,start[0]*(1-v)+end[0]*v,start[1]*(1-v)+end[1]*v,start[2]*(1-v)+end[2]*v,start[3]*(1-v)+end[3]*v)
            self.update()
            time.sleep(0.003)
        self.canv.coords(piece_id,*end)
        self.update()
        self.is_moving = False
        

    # -------- Starting the turns --------

    def initiate_turn(self):
        """Function called at the beginning of each turn and that will either call the ai, or initiate the moves for the bobail or pawns if it is a real player"""
        if self.is_won:
            return 
        self.current_player_label.configure(bg="black" if self.state.player == 1 else "white",fg="black" if self.state.player == 2 else "white")
        self.update()
        if self.state.player in self.real_players:
            if self.state.bobail_to_play:
                self.initiate_bobail_turn()
            else:
                self.color_playable_pawns()
        else:
            self.after(1, self.ai_turn)

    def initiate_bobail_turn(self):
        """Initiate the moves for the bobail"""
        self.add_possible_moves(self.state.possible_plays())

    def color_playable_pawns(self):
        """Colors the playable pawns"""
        self.set_borderwidth_color_playable_pawns(8,"green")

    def uncolor_playable_pawns(self):
        """Uncolors the playable pawns"""
        self.set_borderwidth_color_playable_pawns(6,"grey")

    def set_borderwidth_color_playable_pawns(self, borderwidth, color):
        """Sets the width and the color of the border of the playable pawns"""
        all_pawns = set()
        for move in self.state.possible_plays():
            all_pawns.add(move[0])
        for i,j in all_pawns:
            self.canv.itemconfigure(self.pieces[(i,j)], width=borderwidth, outline=color)
        self.update()
        
    # -------- AI playing --------

    def ai_alone(self):
        """A function that will do some computations on the mcts root node, 10ms by 10ms to be able to think during opponent's turn"""
        if self.is_won:
            return
        if not self.is_moving:
            start = time.time_ns()
            while time.time_ns()-start < 10*10**6:
                self.mcts_root_node.mcts(self.state)
        self.after(1,self.ai_alone)
                       
    def ai_turn(self):
        """The main ai turn. Will do some computations, and then play the move"""
        start = time.time_ns()
        nb_iterations = 0
        while time.time_ns()-start < self.ai_thinking_time*10**9:
            self.mcts_root_node.mcts(self.state)
            nb_iterations += 1
        print("nb nodes =", self.mcts_root_node.nb_nodes(), "nb_iterations =", nb_iterations)
        best_id = self.mcts_root_node.best_id_to_play()
        best_move = self.mcts_root_node.moves[best_id]
        self.play_on_data_and_graphic(best_move)
        self.after(1, self.initiate_turn)

    # -------- Events --------

    def _click(self,event):
        """Event function called when the board is clicked. Will update the possible moves, or play a move"""
        j, i = grid_position(event.x-20, event.y-20)
        if self.state.bobail_to_play:
            move = (self.state.bobail_pos, (i,j))
            if move in self.state.possible_plays():
                self.play_on_data_and_graphic(move)
                self.initiate_turn()
        else:
            if self.state.grid[i][j] == self.state.player:
                moves = self.state.possible_plays()
                restricted_plays = [m for m in moves if m[0]==(i,j)]
                if (i,j) in list(zip(*moves))[0] and len(restricted_plays) != 0:
                    self.remove_possible_moves()
                    self.uncolor_playable_pawns()
                    self.clicked_pawn = (i,j)
                    self.add_possible_moves(restricted_plays)
            else:
                move = (self.clicked_pawn,(i,j))
                if move in self.state.possible_plays():
                    self.play_on_data_and_graphic(move)
                    self.clicked_pawn = None
                    self.initiate_turn()

    def _undo(self):
        """Event function attached to the undo button. Will wait until the ai have finished playing."""
        if len(self.previous_moves) == 0 or self.is_won:
            return
        if self.state.player not in self.real_players:
            self.after(10,self._undo)
            return
        if self.doing_undo:
            return
        self.doing_undo = True
        self.unplay_on_data_and_graphic()
        while len(self.previous_moves) > 0 and self.state.player not in self.real_players:
            self.unplay_on_data_and_graphic()
        self.doing_undo = False
        self.initiate_turn()
                    
    def _exit(self, event):
        """Exit event function"""
        self.quit()

if __name__ == "__main__":
    nb_players = input("Number of real players (0,1 or 2) ? ")
    nb_players = int(nb_players) if nb_players != "" else 0
    if nb_players < 2:
        ai_thinking_time = input("Computation time for AI (in seconds) ? ")
        ai_thinking_time = int(ai_thinking_time) if ai_thinking_time != "" else 1
    else:
        ai_thinking_time = -1
    if nb_players == 1:
        play_first = input("Do you want to play first (Y/n) ? ")
        play_first = play_first != "n"
    else:
        play_first = True
    app = Application(nb_players, ai_thinking_time, play_first)
    app.mainloop()
