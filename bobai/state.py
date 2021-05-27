import random
#from itertools import filter

AROUND = [(i,j) for i in range(-1,2) for j in range(-1,2) if i != 0 or j != 0]
MAX_DEPTH = 10000
bobai_free_moves = {(i,j):[((i,j),(i+k,j+l)) for (k,l) in AROUND if 0 <= i+k < 5 and 0 <= j+l < 5] for i in range(5) for j in range(5)}
pawns_free_moves = {(i,j):[[(i+k*t,j+l*t) for t in range(1,5) if 0 <= i+k*t < 5 and 0 <= j+l*t < 5] for (k,l) in AROUND if 0 <= i+k < 5 and 0 <= j+l < 5] for i in range(5) for j in range(5)}

class State:
    def __init__(self):
        self.player = 1
        self.bobai_to_play = False
        self.grid = [[2,2,2,2,2],[0,0,0,0,0],[0,0,3,0,0],[0,0,0,0,0],[1,1,1,1,1]]
        self.bobai_pos = (2,2)
        self.pawns_positions = [{(4,j) for j in range(5)}, {(0,j) for j in range(5)}]
    def print_state(self):
        """print(f"Turn of player {self.player}, with {['X','O'][self.player-1]}")
        print(f"Play {['a pawn', 'the bobai'][self.bobai_to_play]}")"""
        print("  −−−−−")
        for i in range(5):
            print(f"{4-i}|"+"".join(" XOB"[k] for k in self.grid[i])+"|")
        print("  −−−−−")
        print("  ABCDE")
    def possible_plays(self):
        if self.bobai_to_play:
            return self.possible_bobai_plays()
        else:
            return self.possible_pawn_plays()
    def possible_bobai_plays(self):
        return list(filter(lambda t:self.grid[t[1][0]][t[1][1]]==0, bobai_free_moves[self.bobai_pos]))
    def possible_pawn_plays(self):
        pos = []
        for p in self.pawns_positions[self.player-1]:
            for line in pawns_free_moves[p]:
                t = 0
                l = len(line)
                while t < l and self.grid[line[t][0]][line[t][1]] == 0:
                    t += 1
                if t != 0:
                    pos.append((p,line[t-1]))
        return pos
    def play(self, move):
        if self.bobai_to_play:
            return self.play_bobai(move)
        else:
            return self.play_pawn(move)
    def play_bobai(self, move):
        end = move[1]
        self.grid[end[0]][end[1]] = 3
        self.grid[self.bobai_pos[0]][self.bobai_pos[1]] = 0
        self.bobai_pos = end
        bobai_pos = end
        self.bobai_to_play = False
        if self.bobai_pos[0] == 4:
            return 1
        elif self.bobai_pos[0] == 0:
            return 2
        else:
            return 0
    def play_pawn(self, move):
        start, end = move
        self.grid[start[0]][start[1]] = 0
        self.grid[end[0]][end[1]] = self.player
        self.pawns_positions[self.player-1].remove(start)
        self.pawns_positions[self.player-1].add(end)
        self.player = 3-self.player
        self.bobai_to_play = True
        return 3-self.player if len(self.possible_bobai_plays()) == 0 else 0
    def unplay(self, move):
        if self.bobai_to_play:
            return self.unplay_pawn(move)
        else:
            return self.unplay_bobai(move)
    def unplay_bobai(self, move):
        prev_b_pos, end = move
        self.grid[end[0]][end[1]] = 0
        self.grid[prev_b_pos[0]][prev_b_pos[1]] = 3
        self.bobai_pos = prev_b_pos
        self.bobai_to_play = True
    def unplay_pawn(self, move):
        self.bobai_to_play = False
        self.player = 3-self.player
        start, end = move
        self.grid[start[0]][start[1]] = self.player
        self.grid[end[0]][end[1]] = 0
        self.pawns_positions[self.player-1].add(start)
        self.pawns_positions[self.player-1].remove(end)
    def rollout_rec(self, depth):
        if depth > MAX_DEPTH:
            return 0
        plays = self.possible_plays()
        if len(plays) == 0:
            return -1
        """for move in plays:
            winner = self.play(move)
            if self.player == winner:
                res = 1
            self.unplay(move)"""
        move_played = random.choice(plays)
        winner = self.play(move_played)
        if winner != 0:
            if self.player == winner:
                res = 1
            else:
                res = -1
        else:
            if self.bobai_to_play:
                res = -self.rollout_rec(depth+1)
            else:
                res = self.rollout_rec(depth+1)
        self.unplay(move_played)
        return res
