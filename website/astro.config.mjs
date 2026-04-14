// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import react from '@astrojs/react';
import markdoc from '@astrojs/markdoc';
import cloudflare from '@astrojs/cloudflare';

// https://astro.build/config
export default defineConfig({
	site: 'https://areslib.pages.dev',
	output: 'server',
	adapter: cloudflare({
		imageService: 'cloudflare',
		routes: {
			extend: {
				include: ['/keystatic', '/keystatic/*', '/api/keystatic', '/api/keystatic/*']
			}
		}
	}),
	vite: {
		ssr: {
			external: ['node:path', 'node:fs', 'node:url', 'node:util', 'path', 'fs', 'url', 'util', 'postcss', 'util-deprecate'],
		},
		build: {
			chunkSizeWarningLimit: 2000,
		}
	},
	integrations: [
		starlight({
			title: 'ARESLib Documentation',
			customCss: [
				'./src/styles/custom.css',
			],
			logo: {
				src: './src/assets/ares-logo.png',
			},
			favicon: '/img/ares-logo.png',
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/ARES-23247/ARESLib' }],
			sidebar: [
				{
					label: 'Recruit Training',
					autogenerate: { directory: 'tutorials' },
				},
				{
					label: 'The ARESLib Standard',
					link: '/standards/',
				},
				{
					label: 'API Reference',
					link: 'https://ARES-23247.github.io/ARESLib/javadoc/index.html',
					attrs: { target: '_blank' }
				},
			],
			components: {
				Footer: './src/components/Footer.astro',
				SiteTitle: './src/components/SiteTitle.astro',
				PageTitle: './src/components/PageTitle.astro',
			},
		}),
		react(),
		markdoc()
	],
});
