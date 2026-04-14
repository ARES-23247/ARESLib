import { config, fields, collection } from '@keystatic/core';
import { block, wrapper } from '@keystatic/core/content-components';
import React from 'react';

const starlightSchema = {
	title: fields.slug({ name: { label: 'Title' } }),
	description: fields.text({ label: 'Description', multiline: true }),
	sidebar: fields.object(
		{
			order: fields.integer({ label: 'Sidebar Order' }),
			label: fields.text({ label: 'Sidebar Label' }),
		},
		{ label: 'Sidebar Settings' }
	),
	body: fields.mdx({
		extension: 'mdx',
		components: {
			RuleSection: wrapper({
				label: 'Rule Section',
				schema: {
					num: fields.text({ label: 'Rule Number' }),
					title: fields.text({ label: 'Rule Title' }),
				},
			}),
			CodeComparison: wrapper({
				label: 'Code Comparison',
				schema: {},
			}),
			CodeViolation: wrapper({
				label: 'Code Violation',
				schema: {},
			}),
			CodeStandard: wrapper({
				label: 'Code Standard',
				schema: {},
			}),
			StandardHeader: block({
				label: 'Standard Header',
				schema: {},
			}),
		},
	}),
};

const contentFormat = { contentField: 'body' } as const;

export default config({
	ui: {
		brand: {
			name: 'ARESLib CMS',
			mark: () =>
				React.createElement('img', {
					src: '/img/ares-logo.png',
					height: 28,
					alt: 'ARESLib Logo',
				}),
		},
		navigation: {
			Content: ['pages', 'tutorials'],
		},
	},
	storage: {
		kind: 'github',
		repo: 'ARES-23247/ARESLib',
	},
	collections: {
		pages: collection({
			label: 'Site Pages',
			slugField: 'title',
			path: 'website/src/content/docs/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		tutorials: collection({
			label: 'Recruit Training',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/*',
			format: contentFormat,
			schema: starlightSchema,
		}),
	},
});
