from state import *
from mcts import *
import time
import cProfile

def get_test_input():
    print("Input the rows one by one, with values separated by spaces. The current player has pawns with number 1, the other player is number 2, the bobai is 3, and all the empty cases are 0")
    board = [list(map(int,input("First line : ").split())),list(map(int,input("Second line : ").split())),list(map(int,input("Third line : ").split())),list(map(int,input("Fourth line : ").split())),list(map(int,input("Fifth line : ").split()))]
    state = State()
    state.bobail_to_play = "1"==input("Is it the bobail's turn (0/1) ? ")
    state.grid = board
    state.pawns_positions = [set(),set()]
    for i in range(5):
        for j in range(5):
            if board[i][j] == 1:
                state.pawns_positions[0].add((i,j))
            elif board[i][j] == 2:
                state.pawns_positions[1].add((i,j))
            elif board[i][j] == 3:
                state.bobail_pos = (i,j)
    seconds_to_compute = float(input("For how long should the AI compute (in seconds) ? "))
    depth_to_print = int(input("What depth of moves should be printed (2 or 3 is good) ? "))
    return state, seconds_to_compute, depth_to_print

def evaluate_position(state, seconds_to_compute):
    print(state.possible_bobail_plays())
    mcts_root_node = Node(state.possible_plays())
    start = time.time_ns()
    nb_iterations = 0
    while time.time_ns()-start < seconds_to_compute*10**9:
        mcts_root_node.mcts(state)
        nb_iterations += 1
    print("\nnb nodes =", mcts_root_node.nb_nodes(), "nb_iterations =", nb_iterations)
    return mcts_root_node

def test_single_position():
    state, seconds_to_compute, depth_to_print = get_test_input()
    state.print_state()
    mcts_root_node = evaluate_position(state, seconds_to_compute)
    mcts_root_node.print_scores(0, depth_to_print)
    
if __name__ == "__main__":
    test_single_position()
        

        
