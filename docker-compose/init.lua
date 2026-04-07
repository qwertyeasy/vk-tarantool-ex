box.schema.user.create('spider', { password = 'man', if_not_exists = true })
box.schema.user.grant('spider', 'read,write,execute', 'universe')

box.schema.create_space('KV'):create_index('pk')