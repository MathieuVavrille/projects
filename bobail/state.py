import random
#from itertools import filter

AROUND = [(i,j) for i in range(-1,2) for j in range(-1,2) if i != 0 or j != 0]
MAX_DEPTH = 10000
bobail_free_moves = {(i,j):[((i,j),(i+k,j+l)) for (k,l) in AROUND if 0 <= i+k < 5 and 0 <= j+l < 5] for i in range(5) for j in range(5)}
pawns_free_moves = {(i,j):[[(i+k*t,j+l*t) for t in range(1,5) if 0 <= i+k*t < 5 and 0 <= j+l*t < 5] for (k,l) in AROUND if 0 <= i+k < 5 and 0 <= j+l < 5] for i in range(5) for j in range(5)}

class State:
    def __init__(self):
        self.player = 1
        self.bobail_to_play = False
        self.grid = [[2,2,2,2,2],[0,0,0,0,0],[0,0,3,0,0],[0,0,0,0,0],[1,1,1,1,1]]
        self.bobail_pos = (2,2)
        self.pawns_positions = [{(4,j) for j in range(5)}, {(0,j) for j in range(5)}]
    def print_state(self):
        """print(f"Turn of player {self.player}, with {['X','O'][self.player-1]}")
        print(f"Play {['a pawn', 'the bobail'][self.bobail_to_play]}")"""
        print("  −−−−−")
        for i in range(5):
            print(f"{4-i}|"+"".join(" XOB"[k] for k in self.grid[i])+"|")
        print("  −−−−−")
        print("  ABCDE")
        print(self.pawns_positions)
    def possible_plays(self):
        if self.bobail_to_play:
            return self.possible_bobail_plays()
        else:
            return self.possible_pawn_plays()
    def possible_bobail_plays(self):
        return list(filter(lambda t:self.grid[t[1][0]][t[1][1]]==0, bobail_free_moves[self.bobail_pos]))
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
        if self.bobail_to_play:
            return self.play_bobail(move)
        else:
            return self.play_pawn(move)
    def play_bobail(self, move):
        end = move[1]
        self.grid[end[0]][end[1]] = 3
        self.grid[self.bobail_pos[0]][self.bobail_pos[1]] = 0
        self.bobail_pos = end
        bobail_pos = end
        self.bobail_to_play = False
        if self.bobail_pos[0] == 4:
            return self.player == 1
        elif self.bobail_pos[0] == 0:
            return self.player == 2
        else:
            return None
    def play_pawn(self, move):
        start, end = move
        self.grid[start[0]][start[1]] = 0
        self.grid[end[0]][end[1]] = self.player
        self.pawns_positions[self.player-1].remove(start)
        self.pawns_positions[self.player-1].add(end)
        self.player = 3-self.player
        self.bobail_to_play = True
        return True if len(self.possible_bobail_plays()) == 0 else None
    def unplay(self, move):
        if self.bobail_to_play:
            return self.unplay_pawn(move)
        else:
            return self.unplay_bobail(move)
    def unplay_bobail(self, move):
        prev_b_pos, end = move
        self.grid[end[0]][end[1]] = 0
        self.grid[prev_b_pos[0]][prev_b_pos[1]] = 3
        self.bobail_pos = prev_b_pos
        self.bobail_to_play = True
    def unplay_pawn(self, move):
        self.bobail_to_play = False
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
        interesting_moves = []
        for move in plays:
            winner = self.play(move)
            if winner != None:
                if winner:
                    self.unplay(move)
                    return 1#float("inf")
            else:
                interesting_moves.append(move)
            self.unplay(move)
        if len(interesting_moves) == 0:
            return -1#float("-inf")
        move_played = random.choice(interesting_moves)
        winner = self.play(move_played)
        if winner != None:
            raise ValueError("It should not be possible to be there")
        else:
            if self.bobail_to_play:
                res = max(float("-inf") if len(interesting_moves) == 1 else -1,-self.rollout_rec(depth+1))
            else:
                res = max(float("-inf") if len(interesting_moves) == 1 else -1,self.rollout_rec(depth+1))
        self.unplay(move_played)
        return res
