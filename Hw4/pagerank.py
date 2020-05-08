import networkx as nx

G = nx.read_edgelist("edgeList.txt", create_using=nx.DiGraph())
myPR = nx.pagerank(G, alpha=0.85, personalization=None, max_iter=30, tol=1e-06, nstart=None, weight='weight',dangling=None)

write = 'external_pageRankFile.txt'
f = open(write, 'w')
for id in myPR:
	f.write("/Users/frank/Desktop/572/hw4/dataset/foxnews/"+ id + "=" + str(myPR[id]) + "\n")


f.close()
