import sys, math, json
from collections import Counter

def shannon_entropy(text):
    if not text or len(text) < 10: return 0.0
    freq = Counter(text)
    probs = [count / len(text) for count in freq.values()]
    return -sum(p * math.log2(p) for p in probs if p > 0)

# Get changed files in PR
import subprocess
changed = subprocess.check_output(['git', 'diff', '--name-only', 'origin/${{ github.base_ref }}...']).decode().splitlines()

results = []
total_ent = 0
count = 0
for f in changed:
    if not f or f.endswith(('.png','.jpg','.gif','.bin','.lock')): continue
    try:
        with open(f, 'r', encoding='utf-8', errors='ignore') as fh:
            content = fh.read()
        ent = shannon_entropy(content)
        results.append(f"{f}: {ent:.3f}")
        total_ent += ent
        count += 1
    except: pass

avg = total_ent / count if count else 0
verdict = "✅ Mid-4 beauty detected (thoughtful human code!)" if 4.3 <= avg <= 4.7 else \
          "⚠️  Consider review — entropy outside sweet spot" if avg > 0 else "No source files changed"

with open('/tmp/beauty.json', 'w') as f:
    json.dump({
        "average_entropy": round(avg, 3),
        "verdict": verdict,
        "files": results[:20]  # limit to avoid huge comment
    }, f)
