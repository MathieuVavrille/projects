import math

class Node(object):
    __slots__= "moves","children","total_nb_tests","nb_wins","nb_tests"
    def __init__(self, moves):
        self.moves = moves
        self.children = [None]*len(moves)
        self.total_nb_tests = 0
        self.nb_wins = [0]*len(moves)
        self.nb_tests = [0]*len(moves)
    def nb_nodes(self):
        return 1+sum(c.nb_nodes() for c in self.children if c != None)
    def id_of_move(self,move):
        for i in range(len(self.moves)):
            if self.moves[i] == move:
                return i
        raise ValueError("Wrong move for child")
    def child_with_move(self,move):
        return self.children[self.id_of_move(move)]
    def get_id_to_play(self):
        if len(self.moves) == 0:
            return -1
        if None in self.children:
            return self.children.index(None)
        best_id = 0
        best_value = self.nb_wins[0]/self.nb_tests[0] + math.sqrt(2*math.log(1+self.total_nb_tests)/self.nb_tests[0])
        for i in range(1,len(self.moves)):
            score = self.nb_wins[i]/self.nb_tests[i] + math.sqrt(2*math.log(1+self.total_nb_tests)/self.nb_tests[i])
            if score > best_value:
                best_value = score
                best_id = i
        return best_id
    def best_id_to_play(self):
        return max((float("-inf") if self.nb_tests[i] == 0 else self.nb_wins[i]/self.nb_tests[i],i) for i in range(len(self.moves)))[1]
    def mcts(self, state):
        id_to_play = self.get_id_to_play()
        if id_to_play == -1:
            return float("-inf")
        if math.isinf(self.nb_wins[id_to_play]):
            return self.nb_wins[id_to_play]
        move_played = self.moves[id_to_play]
        winner = state.play(move_played)
        if winner != None:
            self.children[id_to_play] = Node([])
            if winner:
                res = float("inf")
            else:
                res = float("-inf")
        elif self.children[id_to_play] == None:
            self.children[id_to_play] = Node(state.possible_plays())
            if state.bobail_to_play:
                res = -state.rollout_rec(0) # +i -> -i, -i -> +1, x -> -x
            else:
                res = state.rollout_rec(0) # +i -> +i, -i -> -1, x -> x
        else:
            if state.bobail_to_play:
                res = -self.children[id_to_play].mcts(state)
            else:
                res = self.children[id_to_play].mcts(state)
        state.unplay(move_played)
        self.nb_tests[id_to_play] += 1
        self.nb_wins[id_to_play] += res
        self.total_nb_tests += 1
        return max(-1,res)
    def print_scores(self, current_depth, max_depth):
        for i in range(len(self.moves)):
            if self.children[i] != None:
                print(" |"*current_depth, self.moves[i], self.nb_wins[i]/self.nb_tests[i], self.nb_tests[i])
                if current_depth < max_depth and self.nb_tests[i]*20 >= self.total_nb_tests and not math.isinf(self.nb_wins[i]):
                    self.children[i].print_scores(current_depth+1, max_depth)
        
